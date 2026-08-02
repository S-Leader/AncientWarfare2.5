#!/usr/bin/env python3
"""Convert legacy 1.12 .lang files to 1.20.1 JSON without deleting originals."""
from __future__ import annotations
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LANG_ROOT = ROOT / "src" / "main" / "resources" / "assets" / "ancientwarfare" / "lang"

for source in sorted(LANG_ROOT.glob("*.lang")):
    values: dict[str, str] = {}
    duplicates: list[str] = []
    for raw in source.read_text(encoding="utf-8-sig", errors="replace").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        key = key.strip()
        if not key:
            continue
        if key in values:
            duplicates.append(key)
        values[key] = value.replace("\\n", "\n")

    locale = source.stem.lower()
    target = source.with_name(locale + ".json")
    target.write_text(json.dumps(values, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"{source.name} -> {target.name}: {len(values)} keys, {len(duplicates)} duplicates")
