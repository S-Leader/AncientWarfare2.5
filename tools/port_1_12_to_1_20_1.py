#!/usr/bin/env python3
"""Conservative Ancient Warfare 2 MCP 1.12.2 -> Mojmap 1.20.1 first pass.

This script deliberately changes only package moves and method renames with a
clear 1:1 meaning.  It does NOT attempt API redesigns such as registries,
constructors, menus, worldgen, renderers, networking, capabilities, Materials,
or block/entity inheritance.

Run from the migration workspace root with:
    python tools/port_1_12_to_1_20_1.py

Reports are written under migration/.
"""
from __future__ import annotations

import csv
import re
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = ROOT / "src" / "main" / "java"
REPORT_ROOT = ROOT / "migration"

# Exact import package moves that retain substantially the same semantic type.
IMPORT_RENAMES: dict[str, str] = {
    # Core names / geometry.
    "net.minecraft.util.math.BlockPos": "net.minecraft.core.BlockPos",
    "net.minecraft.util.math.ChunkPos": "net.minecraft.world.level.ChunkPos",
    "net.minecraft.util.math.Vec3i": "net.minecraft.core.Vec3i",
    "net.minecraft.util.math.AxisAlignedBB": "net.minecraft.world.phys.AABB",
    "net.minecraft.util.math.Vec3d": "net.minecraft.world.phys.Vec3",
    "net.minecraft.util.math.RayTraceResult": "net.minecraft.world.phys.HitResult",
    "net.minecraft.util.math.MathHelper": "net.minecraft.util.Mth",
    "net.minecraft.util.ResourceLocation": "net.minecraft.resources.ResourceLocation",
    "net.minecraft.util.EnumFacing": "net.minecraft.core.Direction",
    "net.minecraft.util.EnumHand": "net.minecraft.world.InteractionHand",
    "net.minecraft.util.EnumHandSide": "net.minecraft.world.entity.HumanoidArm",
    "net.minecraft.util.EnumActionResult": "net.minecraft.world.InteractionResult",
    "net.minecraft.util.ActionResult": "net.minecraft.world.InteractionResultHolder",
    "net.minecraft.util.SoundCategory": "net.minecraft.sounds.SoundSource",
    "net.minecraft.util.SoundEvent": "net.minecraft.sounds.SoundEvent",
    "net.minecraft.util.NonNullList": "net.minecraft.core.NonNullList",
    "net.minecraft.util.Rotation": "net.minecraft.world.level.block.Rotation",
    "net.minecraft.util.Mirror": "net.minecraft.world.level.block.Mirror",
    "net.minecraft.util.IStringSerializable": "net.minecraft.util.StringRepresentable",

    # World.
    "net.minecraft.world.World": "net.minecraft.world.level.Level",
    "net.minecraft.world.WorldServer": "net.minecraft.server.level.ServerLevel",
    "net.minecraft.world.IBlockAccess": "net.minecraft.world.level.BlockGetter",
    "net.minecraft.world.EnumDifficulty": "net.minecraft.world.Difficulty",
    "net.minecraft.world.GameType": "net.minecraft.world.level.GameType",
    "net.minecraft.world.chunk.Chunk": "net.minecraft.world.level.chunk.LevelChunk",
    "net.minecraft.world.biome.Biome": "net.minecraft.world.level.biome.Biome",
    "net.minecraft.world.storage.WorldSavedData": "net.minecraft.world.level.saveddata.SavedData",

    # Block/state properties. No Material, BlockContainer, IGrowable, or other
    # inheritance-changing substitutions are performed here.
    "net.minecraft.block.Block": "net.minecraft.world.level.block.Block",
    "net.minecraft.block.SoundType": "net.minecraft.world.level.block.SoundType",
    "net.minecraft.block.state.IBlockState": "net.minecraft.world.level.block.state.BlockState",
    "net.minecraft.block.properties.IProperty": "net.minecraft.world.level.block.state.properties.Property",
    "net.minecraft.block.properties.PropertyBool": "net.minecraft.world.level.block.state.properties.BooleanProperty",
    "net.minecraft.block.properties.PropertyDirection": "net.minecraft.world.level.block.state.properties.DirectionProperty",
    "net.minecraft.block.properties.PropertyEnum": "net.minecraft.world.level.block.state.properties.EnumProperty",
    "net.minecraft.block.properties.PropertyInteger": "net.minecraft.world.level.block.state.properties.IntegerProperty",

    # Entities.
    "net.minecraft.entity.Entity": "net.minecraft.world.entity.Entity",
    "net.minecraft.entity.EntityLivingBase": "net.minecraft.world.entity.LivingEntity",
    "net.minecraft.entity.EntityLiving": "net.minecraft.world.entity.Mob",
    "net.minecraft.entity.EntityCreature": "net.minecraft.world.entity.PathfinderMob",
    "net.minecraft.entity.EntityAgeable": "net.minecraft.world.entity.AgeableMob",
    "net.minecraft.entity.MoverType": "net.minecraft.world.entity.MoverType",
    "net.minecraft.entity.player.EntityPlayer": "net.minecraft.world.entity.player.Player",
    "net.minecraft.entity.player.EntityPlayerMP": "net.minecraft.server.level.ServerPlayer",
    "net.minecraft.entity.item.EntityItem": "net.minecraft.world.entity.item.ItemEntity",
    "net.minecraft.entity.item.EntityXPOrb": "net.minecraft.world.entity.ExperienceOrb",
    "net.minecraft.entity.passive.EntityAnimal": "net.minecraft.world.entity.animal.Animal",
    "net.minecraft.entity.passive.EntityVillager": "net.minecraft.world.entity.npc.Villager",
    "net.minecraft.entity.monster.EntityZombie": "net.minecraft.world.entity.monster.Zombie",
    "net.minecraft.entity.monster.EntitySkeleton": "net.minecraft.world.entity.monster.Skeleton",
    "net.minecraft.entity.monster.EntityEnderman": "net.minecraft.world.entity.monster.EnderMan",
    "net.minecraft.entity.monster.EntityGuardian": "net.minecraft.world.entity.monster.Guardian",
    "net.minecraft.entity.ai.EntityAIBase": "net.minecraft.world.entity.ai.goal.Goal",
    "net.minecraft.entity.ai.EntityAITasks": "net.minecraft.world.entity.ai.goal.GoalSelector",
    "net.minecraft.entity.ai.EntityLookHelper": "net.minecraft.world.entity.ai.control.LookControl",
    "net.minecraft.entity.ai.EntityMoveHelper": "net.minecraft.world.entity.ai.control.MoveControl",
    "net.minecraft.entity.ai.EntitySenses": "net.minecraft.world.entity.ai.sensing.Sensing",
    "net.minecraft.entity.ai.attributes.IAttribute": "net.minecraft.world.entity.ai.attributes.Attribute",
    "net.minecraft.entity.ai.attributes.IAttributeInstance": "net.minecraft.world.entity.ai.attributes.AttributeInstance",
    "net.minecraft.entity.ai.attributes.AttributeModifier": "net.minecraft.world.entity.ai.attributes.AttributeModifier",

    # Items/inventory.
    "net.minecraft.item.Item": "net.minecraft.world.item.Item",
    "net.minecraft.item.ItemStack": "net.minecraft.world.item.ItemStack",
    "net.minecraft.item.ItemBlock": "net.minecraft.world.item.BlockItem",
    "net.minecraft.item.ItemFood": "net.minecraft.world.item.Item",
    "net.minecraft.item.EnumAction": "net.minecraft.world.item.UseAnim",
    "net.minecraft.inventory.Container": "net.minecraft.world.inventory.AbstractContainerMenu",
    "net.minecraft.inventory.Slot": "net.minecraft.world.inventory.Slot",
    "net.minecraft.inventory.IInventory": "net.minecraft.world.Container",
    "net.minecraft.inventory.InventoryBasic": "net.minecraft.world.SimpleContainer",
    "net.minecraft.inventory.EntityEquipmentSlot": "net.minecraft.world.entity.EquipmentSlot",

    # NBT.
    "net.minecraft.nbt.NBTBase": "net.minecraft.nbt.Tag",
    "net.minecraft.nbt.NBTTagCompound": "net.minecraft.nbt.CompoundTag",
    "net.minecraft.nbt.NBTTagList": "net.minecraft.nbt.ListTag",
    "net.minecraft.nbt.NBTTagString": "net.minecraft.nbt.StringTag",
    "net.minecraft.nbt.NBTTagInt": "net.minecraft.nbt.IntTag",
    "net.minecraft.nbt.NBTTagLong": "net.minecraft.nbt.LongTag",
    "net.minecraft.nbt.NBTTagShort": "net.minecraft.nbt.ShortTag",
    "net.minecraft.nbt.NBTTagByte": "net.minecraft.nbt.ByteTag",
    "net.minecraft.nbt.NBTTagFloat": "net.minecraft.nbt.FloatTag",
    "net.minecraft.nbt.NBTTagDouble": "net.minecraft.nbt.DoubleTag",
    "net.minecraft.nbt.NBTTagIntArray": "net.minecraft.nbt.IntArrayTag",
    "net.minecraft.nbt.NBTTagByteArray": "net.minecraft.nbt.ByteArrayTag",
    "net.minecraft.nbt.NBTUtil": "net.minecraft.nbt.NbtUtils",
    "net.minecraft.nbt.CompressedStreamTools": "net.minecraft.nbt.NbtIo",
    "net.minecraft.nbt.JsonToNBT": "net.minecraft.nbt.TagParser",

    # Block entities / constants.
    "net.minecraft.tileentity.TileEntity": "net.minecraft.world.level.block.entity.BlockEntity",
    "net.minecraft.tileentity.TileEntityChest": "net.minecraft.world.level.block.entity.ChestBlockEntity",
    "net.minecraft.init.Blocks": "net.minecraft.world.level.block.Blocks",
    "net.minecraft.init.Items": "net.minecraft.world.item.Items",
    "net.minecraft.init.SoundEvents": "net.minecraft.sounds.SoundEvents",
    "net.minecraft.init.Enchantments": "net.minecraft.world.item.enchantment.Enchantments",
    "net.minecraft.init.MobEffects": "net.minecraft.world.effect.MobEffects",
    "net.minecraft.enchantment.EnchantmentHelper": "net.minecraft.world.item.enchantment.EnchantmentHelper",
    "net.minecraft.potion.Potion": "net.minecraft.world.effect.MobEffect",
    "net.minecraft.potion.PotionEffect": "net.minecraft.world.effect.MobEffectInstance",

    # Networking/data sync.
    "net.minecraft.network.PacketBuffer": "net.minecraft.network.FriendlyByteBuf",
    "net.minecraft.network.NetworkManager": "net.minecraft.network.Connection",
    "net.minecraft.network.datasync.DataParameter": "net.minecraft.network.syncher.EntityDataAccessor",
    "net.minecraft.network.datasync.DataSerializers": "net.minecraft.network.syncher.EntityDataSerializers",
    "net.minecraft.network.datasync.EntityDataManager": "net.minecraft.network.syncher.SynchedEntityData",
    "net.minecraft.network.play.server.SPacketUpdateTileEntity": "net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket",

    # Client names whose role stayed recognisable. Rendering implementations
    # still require hand migration and are reported separately.
    "net.minecraft.client.gui.FontRenderer": "net.minecraft.client.gui.Font",
    "net.minecraft.client.entity.EntityPlayerSP": "net.minecraft.client.player.LocalPlayer",
    "net.minecraft.client.multiplayer.WorldClient": "net.minecraft.client.multiplayer.ClientLevel",
    "net.minecraft.client.renderer.BufferBuilder": "com.mojang.blaze3d.vertex.BufferBuilder",
    "net.minecraft.client.renderer.Tessellator": "com.mojang.blaze3d.vertex.Tesselator",
    "net.minecraft.client.renderer.texture.TextureAtlasSprite": "net.minecraft.client.renderer.texture.TextureAtlasSprite",
    "net.minecraft.client.resources.I18n": "net.minecraft.client.resources.language.I18n",
    "net.minecraft.client.settings.KeyBinding": "net.minecraft.client.KeyMapping",
    "net.minecraft.client.util.ITooltipFlag": "net.minecraft.world.item.TooltipFlag",

    # Forge package moves.
    "net.minecraftforge.fml.common.eventhandler.SubscribeEvent": "net.minecraftforge.eventbus.api.SubscribeEvent",
    "net.minecraftforge.fml.common.eventhandler.EventPriority": "net.minecraftforge.eventbus.api.EventPriority",
    "net.minecraftforge.fml.relauncher.Side": "net.minecraftforge.api.distmarker.Dist",
    "net.minecraftforge.fml.relauncher.SideOnly": "net.minecraftforge.api.distmarker.OnlyIn",
    "net.minecraftforge.fml.common.gameevent.TickEvent": "net.minecraftforge.event.TickEvent",
    "net.minecraftforge.fml.common.gameevent.PlayerEvent": "net.minecraftforge.event.entity.player.PlayerEvent",
    "net.minecraftforge.event.entity.EntityJoinWorldEvent": "net.minecraftforge.event.entity.EntityJoinLevelEvent",
    "net.minecraftforge.event.world.WorldEvent": "net.minecraftforge.event.level.LevelEvent",
    "net.minecraftforge.event.world.BlockEvent": "net.minecraftforge.event.level.BlockEvent",
    "net.minecraftforge.fml.common.registry.ForgeRegistries": "net.minecraftforge.registries.ForgeRegistries",
    "net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData": "net.minecraftforge.entity.IEntityAdditionalSpawnData",
}

# Token renames are applied only when their old import appeared in the same file.
TYPE_TOKENS: dict[str, str] = {
    "BlockPos": "BlockPos",
    "ChunkPos": "ChunkPos",
    "Vec3i": "Vec3i",
    "AxisAlignedBB": "AABB",
    "Vec3d": "Vec3",
    "RayTraceResult": "HitResult",
    "MathHelper": "Mth",
    "EnumFacing": "Direction",
    "EnumHand": "InteractionHand",
    "EnumHandSide": "HumanoidArm",
    "EnumActionResult": "InteractionResult",
    "ActionResult": "InteractionResultHolder",
    "SoundCategory": "SoundSource",
    "IStringSerializable": "StringRepresentable",
    "WorldServer": "ServerLevel",
    "World": "Level",
    "IBlockAccess": "BlockGetter",
    "EnumDifficulty": "Difficulty",
    "IBlockState": "BlockState",
    "IProperty": "Property",
    "PropertyBool": "BooleanProperty",
    "PropertyDirection": "DirectionProperty",
    "PropertyEnum": "EnumProperty",
    "PropertyInteger": "IntegerProperty",
    "EntityLivingBase": "LivingEntity",
    "EntityLiving": "Mob",
    "EntityCreature": "PathfinderMob",
    "EntityAgeable": "AgeableMob",
    "EntityPlayerMP": "ServerPlayer",
    "EntityPlayer": "Player",
    "EntityItem": "ItemEntity",
    "EntityXPOrb": "ExperienceOrb",
    "EntityAnimal": "Animal",
    "EntityVillager": "Villager",
    "EntityZombie": "Zombie",
    "EntitySkeleton": "Skeleton",
    "EntityEnderman": "EnderMan",
    "EntityGuardian": "Guardian",
    "EntityAIBase": "Goal",
    "EntityAITasks": "GoalSelector",
    "EntityLookHelper": "LookControl",
    "EntityMoveHelper": "MoveControl",
    "EntitySenses": "Sensing",
    "IAttributeInstance": "AttributeInstance",
    "IAttribute": "Attribute",
    "ItemBlock": "BlockItem",
    "EnumAction": "UseAnim",
    "Container": "AbstractContainerMenu",
    "IInventory": "Container",
    "InventoryBasic": "SimpleContainer",
    "EntityEquipmentSlot": "EquipmentSlot",
    "NBTTagCompound": "CompoundTag",
    "NBTTagList": "ListTag",
    "NBTTagString": "StringTag",
    "NBTTagInt": "IntTag",
    "NBTTagLong": "LongTag",
    "NBTTagShort": "ShortTag",
    "NBTTagByte": "ByteTag",
    "NBTTagFloat": "FloatTag",
    "NBTTagDouble": "DoubleTag",
    "NBTTagIntArray": "IntArrayTag",
    "NBTTagByteArray": "ByteArrayTag",
    "NBTBase": "Tag",
    "NBTUtil": "NbtUtils",
    "CompressedStreamTools": "NbtIo",
    "JsonToNBT": "TagParser",
    "TileEntityChest": "ChestBlockEntity",
    "TileEntity": "BlockEntity",
    "PacketBuffer": "FriendlyByteBuf",
    "NetworkManager": "Connection",
    "DataParameter": "EntityDataAccessor",
    "DataSerializers": "EntityDataSerializers",
    "EntityDataManager": "SynchedEntityData",
    "SPacketUpdateTileEntity": "ClientboundBlockEntityDataPacket",
    "FontRenderer": "Font",
    "EntityPlayerSP": "LocalPlayer",
    "WorldClient": "ClientLevel",
    "Tessellator": "Tesselator",
    "KeyBinding": "KeyMapping",
    "ITooltipFlag": "TooltipFlag",
    "SideOnly": "OnlyIn",
    "Side": "Dist",
    "EntityJoinWorldEvent": "EntityJoinLevelEvent",
    "WorldEvent": "LevelEvent",
}

# Exact call/field renames with no argument reordering.
TEXT_RENAMES: tuple[tuple[str, str], ...] = (
    (".isRemote", ".isClientSide"),
    (".getEntityId()", ".getId()"),
    (".getUniqueID()", ".getUUID()"),
    (".getAttackTarget()", ".getTarget()"),
    (".setAttackTarget(", ".setTarget("),
    (".getNavigator()", ".getNavigation()"),
    (".getEntitySenses()", ".getSensing()"),
    (".getLookHelper()", ".getLookControl()"),
    (".getMoveHelper()", ".getMoveControl()"),
    (".getHeldItemMainhand()", ".getMainHandItem()"),
    (".getHeldItemOffhand()", ".getOffhandItem()"),
    (".getPositionVector()", ".position()"),
    (".getEntityBoundingBox()", ".getBoundingBox()"),
    (".getDistanceSq(", ".distanceToSqr("),
    (".setDead()", ".discard()"),
    (".isEntityAlive()", ".isAlive()"),
    (".markDirty()", ".setChanged()"),
    (".getDefaultState()", ".defaultBlockState()"),
    (".spawnEntity(", ".addFreshEntity("),
    (".getTotalWorldTime()", ".getGameTime()"),
    (".getWorldTime()", ".getDayTime()"),
    (".hasTagCompound()", ".hasTag()"),
    (".getTagCompound()", ".getTag()"),
    (".setTagCompound(", ".setTag("),
    (".getItemDamage()", ".getDamageValue()"),
    (".setItemDamage(", ".setDamageValue("),
    (".getMaxDamage()", ".getMaxDamage()"),
)

OLD_IMPORT_PREFIXES = (
    "net.minecraft.block.",
    "net.minecraft.client.gui.inventory.",
    "net.minecraft.client.renderer.tileentity.",
    "net.minecraft.command.",
    "net.minecraft.entity.",
    "net.minecraft.inventory.",
    "net.minecraft.item.",
    "net.minecraft.network.datasync.",
    "net.minecraft.network.play.",
    "net.minecraft.tileentity.",
    "net.minecraft.util.math.",
    "net.minecraft.util.text.",
    "net.minecraft.world.",
    "net.minecraftforge.fml.common.event.",
    "net.minecraftforge.fml.common.registry.",
    "net.minecraftforge.fml.relauncher.",
    "electroblob.wizardry.",
    "com.feed_the_beast.ftblib.",
)


def migrate_file(path: Path) -> tuple[bool, Counter[str]]:
    original = path.read_text(encoding="utf-8")
    text = original
    changes: Counter[str] = Counter()

    imported_old_types: set[str] = set()
    for old in IMPORT_RENAMES:
        if f"import {old};" in original:
            imported_old_types.add(old.rsplit(".", 1)[-1])

    # Rename type tokens only outside import declarations. Unique placeholders
    # prevent one converted type from being converted by a later rule.
    token_plan: list[tuple[str, str, str]] = []
    for index, old_simple in enumerate(sorted(imported_old_types, key=len, reverse=True)):
        new_simple = TYPE_TOKENS.get(old_simple)
        if not new_simple or new_simple == old_simple:
            continue
        token_plan.append((f"__AW20_TYPE_{index}_{old_simple}__", new_simple, old_simple))

    migrated_lines: list[str] = []
    for line in text.splitlines(keepends=True):
        if line.lstrip().startswith("import "):
            migrated_lines.append(line)
            continue
        for placeholder, _new_simple, old_simple in token_plan:
            line, count = re.subn(rf"\b{re.escape(old_simple)}\b", placeholder, line)
            if count:
                changes[f"type:{old_simple}->{TYPE_TOKENS[old_simple]}"] += count
        migrated_lines.append(line)
    text = "".join(migrated_lines)
    for placeholder, new_simple, _old_simple in token_plan:
        text = text.replace(placeholder, new_simple)

    # Import declarations are replaced only after source tokens, so a newly
    # introduced simple name cannot be mistaken for a legacy type.
    for old, new in IMPORT_RENAMES.items():
        old_line = f"import {old};"
        count = text.count(old_line)
        if count:
            text = text.replace(old_line, f"import {new};")
            changes[f"import:{old}->{new}"] += count

    for old, new in TEXT_RENAMES:
        count = text.count(old)
        if count:
            text = text.replace(old, new)
            changes[f"call:{old}->{new}"] += count

    # Forge 1.20.1 mod-presence API. Exact expression only.
    old_loader = "Loader.isModLoaded("
    if old_loader in text:
        text = text.replace(old_loader, "ModList.get().isLoaded(")
        text = text.replace("import net.minecraftforge.fml.common.Loader;", "import net.minecraftforge.fml.ModList;")
        changes["api:Loader.isModLoaded->ModList.get().isLoaded"] += original.count(old_loader)

    if text != original:
        path.write_text(text, encoding="utf-8", newline="\n")
        return True, changes
    return False, changes


def unresolved_imports() -> list[tuple[str, int, str]]:
    rows: list[tuple[str, int, str]] = []
    import_re = re.compile(r"^import\s+([^;]+);", re.MULTILINE)
    for path in sorted(JAVA_ROOT.rglob("*.java")):
        text = path.read_text(encoding="utf-8", errors="replace")
        for match in import_re.finditer(text):
            imp = match.group(1)
            if imp.startswith(OLD_IMPORT_PREFIXES):
                line = text.count("\n", 0, match.start()) + 1
                rows.append((path.relative_to(ROOT).as_posix(), line, imp))
    return rows


def dependency_usage() -> list[tuple[str, str, int]]:
    needles = {
        "CodeChickenLib": "codechicken.",
        "Wizardry 1.12 API": "electroblob.wizardry.",
        "FTB Lib 1.12 API": "com.feed_the_beast.ftblib.",
        "Forge 1.12 lifecycle": "net.minecraftforge.fml.common.event.FML",
        "Forge 1.12 annotation": "net.minecraftforge.fml.common.Mod",
        "Forge 1.12 proxy": "net.minecraftforge.fml.common.SidedProxy",
    }
    rows: list[tuple[str, str, int]] = []
    for path in sorted(JAVA_ROOT.rglob("*.java")):
        text = path.read_text(encoding="utf-8", errors="replace")
        for category, needle in needles.items():
            count = text.count(needle)
            if count:
                rows.append((category, path.relative_to(ROOT).as_posix(), count))
    return rows


def main() -> None:
    REPORT_ROOT.mkdir(parents=True, exist_ok=True)
    changed_rows: list[tuple[str, int, str]] = []
    total_changes: Counter[str] = Counter()

    for path in sorted(JAVA_ROOT.rglob("*.java")):
        changed, details = migrate_file(path)
        if changed:
            total = sum(details.values())
            changed_rows.append((path.relative_to(ROOT).as_posix(), total, "; ".join(f"{k}={v}" for k, v in sorted(details.items()))))
            total_changes.update(details)

    mechanical_report = REPORT_ROOT / "mechanical_changes.csv"
    # Preserve the first-run audit on idempotent reruns. Re-running the script
    # on an already migrated tree must not erase the record of prior changes.
    if changed_rows or not mechanical_report.exists():
        with mechanical_report.open("w", encoding="utf-8", newline="") as f:
            writer = csv.writer(f)
            writer.writerow(("file", "replacement_count", "details"))
            writer.writerows(changed_rows)
    else:
        print(f"Mechanical report preserved: {mechanical_report}")

    unresolved = unresolved_imports()
    with (REPORT_ROOT / "unresolved_legacy_imports.csv").open("w", encoding="utf-8", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(("file", "line", "import"))
        writer.writerows(unresolved)

    deps = dependency_usage()
    with (REPORT_ROOT / "dependency_usage.csv").open("w", encoding="utf-8", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(("category", "file", "occurrences"))
        writer.writerows(deps)

    print(f"Changed Java files: {len(changed_rows)}")
    print(f"Mechanical replacements: {sum(total_changes.values())}")
    print(f"Unresolved legacy imports: {len(unresolved)}")
    print(f"Dependency-usage rows: {len(deps)}")
    print(f"Reports: {REPORT_ROOT}")


if __name__ == "__main__":
    main()
