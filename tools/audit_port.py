#!/usr/bin/env python3
"""Generate a repeatable Ancient Warfare 1.12.2 -> Forge 1.20.1 migration audit.

This script does not modify Java or resource files. It records remaining legacy
APIs and resource formats so the port can be worked through subsystem-by-subsystem.
"""
from __future__ import annotations

import csv
import json
import re
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = ROOT / "src/main/java"
RES_ROOT = ROOT / "src/main/resources"
OUT = ROOT / "migration"
OUT.mkdir(parents=True, exist_ok=True)

LEGACY_PATTERNS = {
    "lifecycle/proxy": [
        r"net\.minecraftforge\.fml\.common\.event\.FML",
        r"net\.minecraftforge\.fml\.common\.SidedProxy",
        r"net\.minecraftforge\.fml\.common\.Mod\.(?:EventHandler|Instance)",
    ],
    "registry": [
        r"RegistryEvent", r"GameRegistry", r"ObjectHolder", r"EntityRegistry",
        r"ForgeRegistries\.[A-Z_]+\.register",
    ],
    "network": [
        r"SimpleNetworkWrapper", r"simpleimpl\.(?:IMessage|IMessageHandler)",
        r"NetworkRegistry\.INSTANCE\.newSimpleChannel",
    ],
    "gui/menu": [
        r"GuiContainer", r"IGuiHandler", r"GuiScreen", r"GuiButton",
        r"ContainerPlayer", r"openGui\(",
    ],
    "entity/ai": [
        r"net\.minecraft\.entity\.", r"EntityAI[A-Z]", r"SharedMonsterAttributes",
        r"setMutexBits\(", r"shouldExecute\(", r"updateTask\(",
    ],
    "block/blockstate": [
        r"net\.minecraft\.block\.", r"IBlockState", r"BlockStateContainer",
        r"createBlockState\(", r"getStateFromMeta\(", r"getMetaFromState\(",
    ],
    "block-entity": [
        r"net\.minecraft\.tileentity\.", r"TileEntitySpecialRenderer",
        r"ITickable", r"createNewTileEntity\(",
    ],
    "rendering": [
        r"GlStateManager", r"Tessellator", r"BufferBuilder", r"TRSRTransformation",
        r"IModelState", r"IBakedModel", r"ModelLoader\.setCustom",
    ],
    "worldgen": [
        r"IWorldGenerator", r"BiomeDictionary", r"WorldGenerator",
        r"PopulateChunkEvent", r"DecorateBiomeEvent",
    ],
    "commands": [r"ICommandSender", r"CommandBase", r"FMLServerStartingEvent"],
    "capability/item-handler": [r"IInventory", r"InventoryBasic", r"CombinedInvWrapper", r"InvWrapper"],
    "wizardry-legacy": [r"electroblob\.wizardry"],
    "codechickenlib": [r"codechicken\.lib"],
    "ftb-legacy": [r"com\.feed_the_beast\.ftblib", r"ForgeTeam", r"ForgePlayer", r"Universe"],
}


def module_for(path: Path) -> str:
    parts = path.relative_to(JAVA_ROOT).parts
    try:
        i = parts.index("ancientwarfare")
        return parts[i + 1]
    except (ValueError, IndexError):
        return "other"


def first_line(text: str, pattern: str) -> int:
    rx = re.compile(pattern)
    for i, line in enumerate(text.splitlines(), 1):
        if rx.search(line):
            return i
    return 0


java_rows: list[dict[str, object]] = []
category_totals = Counter()
module_totals = Counter()
module_lines = Counter()
module_categories: dict[str, Counter[str]] = defaultdict(Counter)

for path in sorted(JAVA_ROOT.rglob("*.java")):
    text = path.read_text(encoding="utf-8", errors="replace")
    module = module_for(path)
    module_totals[module] += 1
    module_lines[module] += text.count("\n") + 1
    for category, patterns in LEGACY_PATTERNS.items():
        matched = [p for p in patterns if re.search(p, text)]
        if not matched:
            continue
        category_totals[category] += 1
        module_categories[module][category] += 1
        java_rows.append({
            "module": module,
            "category": category,
            "file": path.relative_to(ROOT).as_posix(),
            "first_line": min(x for x in (first_line(text, p) for p in matched) if x),
            "matched_patterns": " | ".join(matched),
        })

with (OUT / "port_audit_java.csv").open("w", newline="", encoding="utf-8") as f:
    writer = csv.DictWriter(f, fieldnames=["module", "category", "file", "first_line", "matched_patterns"])
    writer.writeheader()
    writer.writerows(java_rows)

resource_rows: list[dict[str, object]] = []
resource_issue_totals = Counter()

for path in sorted(RES_ROOT.rglob("*")):
    if not path.is_file():
        continue
    rel = path.relative_to(ROOT).as_posix()
    suffix = path.suffix.lower()
    issues: list[str] = []
    notes: list[str] = []
    text = ""
    if suffix in {".json", ".lang", ".mcmeta", ".cfg", ".info"}:
        text = path.read_text(encoding="utf-8", errors="replace")

    if suffix == ".lang":
        issues.append("legacy_lang")
        notes.append("JSON language file generated alongside; retain until key remap is verified")
    if path.name == "mcmod.info":
        issues.append("legacy_mcmod_info")
        notes.append("replaced by META-INF/mods.toml")
    if path.name.endswith("_at.cfg"):
        issues.append("legacy_access_transformer")
        notes.append("class/member names require 1.20.1 Mojmap remap")
    if "/assets/" in f"/{rel}" and "/loot_tables/" in rel:
        issues.append("loot_table_under_assets")
        notes.append("must be converted to modern loot schema before moving under data/<namespace>/loot_tables")
    if suffix == ".json" and '"forge_marker"' in text:
        issues.append("forge_marker_blockstate")
        notes.append("Forge 1.12 extended blockstate/model format requires model rewrite")
    if suffix == ".json" and ('"set_data"' in text or '"minecraft:set_data"' in text):
        issues.append("legacy_loot_function_set_data")
        notes.append("replace metadata with modern item/block IDs or set_nbt/components as appropriate")
    if suffix == ".json" and re.search(r'"item"\s*:\s*"(?:ancientwarfare|minecraft):[^"\s]+"', text):
        # This may be valid, but flag old colonless AW item variants separately below.
        pass
    if suffix == ".json" and re.search(r'"(?:item|name)"\s*:\s*"[^":/]+"', text):
        issues.append("possibly_unnamespaced_registry_id")
        notes.append("verify every registry ID after DeferredRegister conversion")
    if suffix in {".obj", ".mtl"}:
        issues.append("legacy_obj_model")
        notes.append("verify Forge OBJ loader/model event registration on 1.20.1")
    if suffix == ".x3d":
        issues.append("x3d_model")
        notes.append("no vanilla/Forge 1.20.1 loader; convert or retain CCL geometry loader")
    if suffix == ".json" and "/recipes/" in rel and "/assets/" in f"/{rel}":
        issues.append("recipe_under_assets")
        notes.append("custom AW recipe assets require serializer/reload-listener decision; do not blindly move")
    if suffix == ".nbt" and "/structures/" in rel and "/assets/" in f"/{rel}":
        issues.append("structure_nbt_under_assets")
        notes.append("move only after structure loader IDs and data paths are ported")

    for issue in issues:
        resource_issue_totals[issue] += 1
        resource_rows.append({
            "issue": issue,
            "file": rel,
            "note": "; ".join(dict.fromkeys(notes)),
        })

with (OUT / "port_audit_resources.csv").open("w", newline="", encoding="utf-8") as f:
    writer = csv.DictWriter(f, fieldnames=["issue", "file", "note"])
    writer.writeheader()
    writer.writerows(resource_rows)

summary = {
    "java_files": sum(module_totals.values()),
    "java_lines": sum(module_lines.values()),
    "resource_files": sum(1 for p in RES_ROOT.rglob("*") if p.is_file()),
    "modules": {
        module: {
            "files": module_totals[module],
            "lines": module_lines[module],
            "legacy_categories": dict(sorted(module_categories[module].items())),
        }
        for module in sorted(module_totals)
    },
    "java_category_file_counts": dict(category_totals.most_common()),
    "resource_issue_file_counts": dict(resource_issue_totals.most_common()),
    "notes": [
        "Counts are file counts, not compiler error counts.",
        "A file may appear in multiple categories.",
        "The audit is intentionally conservative and does not claim compileability.",
    ],
}
(OUT / "port_audit_summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

print(json.dumps(summary, ensure_ascii=False, indent=2))
