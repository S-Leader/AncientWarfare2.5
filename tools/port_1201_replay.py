#!/usr/bin/env python3
"""Repeatable mechanical portion of the Forge 1.12 -> 1.20.1 migration.

This script intentionally performs only mappings that are context independent.
Architectural changes (registries, menus, model data, networking and worldgen)
remain explicit Java edits so that their behavior can be reviewed.
"""

from __future__ import annotations

import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = ROOT / "src/main/java"
JAVA_NON_CODE = re.compile(
    r'("(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*(?:\n|$)|/\*.*?\*/)',
    re.DOTALL,
)


LITERALS = {
    # Minecraft package moves.
    "net.minecraft.block.material.Material": "net.shadowmage.ancientwarfare.core.compat.LegacyMaterial",
    "net.minecraft.creativetab.CreativeTabs": "net.shadowmage.ancientwarfare.core.LegacyCreativeTab",
    "net.minecraft.util.text.TextComponentTranslation": "net.minecraft.network.chat.Component",
    "net.minecraft.util.text.TextComponentString": "net.minecraft.network.chat.Component",
    "net.minecraft.util.text.ITextComponent": "net.minecraft.network.chat.Component",
    "net.minecraft.util.text.TextFormatting": "net.minecraft.ChatFormatting",
    "net.minecraft.util.JsonUtils": "net.minecraft.util.GsonHelper",
    "net.minecraft.entity.SharedMonsterAttributes": "net.minecraft.world.entity.ai.attributes.Attributes",
    "net.minecraft.util.DamageSource": "net.minecraft.world.damagesource.DamageSource",
    "net.minecraft.scoreboard.Team": "net.minecraft.world.scores.Team",
    "net.minecraft.scoreboard.ScorePlayerTeam": "net.minecraft.world.scores.PlayerTeam",
    "net.minecraft.entity.IRangedAttackMob": "net.minecraft.world.entity.monster.RangedAttackMob",
    "net.minecraft.entity.INpc": "net.minecraft.world.entity.npc.Npc",
    "net.minecraft.entity.passive.AbstractHorse": "net.minecraft.world.entity.animal.horse.AbstractHorse",
    "net.minecraft.entity.passive.EntityHorse": "net.minecraft.world.entity.animal.horse.Horse",
    "net.minecraft.entity.passive.EntityPig": "net.minecraft.world.entity.animal.Pig",
    "net.minecraft.entity.monster.IMob": "net.minecraft.world.entity.monster.Enemy",
    "net.minecraft.entity.projectile.EntityArrow": "net.minecraft.world.entity.projectile.AbstractArrow",
    "net.minecraft.entity.projectile.EntityPotion": "net.minecraft.world.entity.projectile.ThrownPotion",
    "net.minecraft.entity.EntityList": "net.minecraft.core.registries.BuiltInRegistries",
    "net.minecraft.entity.ai.EntityAISwimming": "net.minecraft.world.entity.ai.goal.FloatGoal",
    "net.minecraft.entity.ai.EntityAIWatchClosest2": "net.minecraft.world.entity.ai.goal.LookAtPlayerGoal",
    "net.minecraft.entity.ai.EntityAIWatchClosest": "net.minecraft.world.entity.ai.goal.LookAtPlayerGoal",
    "net.minecraft.entity.ai.EntityAINearestAttackableTarget": "net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal",
    "net.minecraft.entity.ai.EntityAITarget": "net.minecraft.world.entity.ai.goal.target.TargetGoal",
    "net.minecraft.entity.ai.EntityAIHurtByTarget": "net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal",
    "net.minecraft.entity.ai.RandomPositionGenerator": "net.minecraft.world.entity.ai.util.DefaultRandomPos",
    "net.minecraft.pathfinding.PathNavigateGround": "net.minecraft.world.entity.ai.navigation.GroundPathNavigation",
    "net.minecraft.pathfinding.WalkNodeProcessor": "net.minecraft.world.level.pathfinder.WalkNodeEvaluator",
    "net.minecraft.pathfinding.PathNodeType": "net.minecraft.world.level.pathfinder.BlockPathTypes",
    "net.minecraft.pathfinding.PathPoint": "net.minecraft.world.level.pathfinder.Node",
    "net.minecraft.pathfinding.PathFinder": "net.minecraft.world.level.pathfinder.PathFinder",
    "net.minecraft.pathfinding.Path": "net.minecraft.world.level.pathfinder.Path",
    "net.minecraft.inventory.InventoryCrafting": "net.minecraft.world.inventory.CraftingContainer",
    "net.minecraft.inventory.InventoryCraftResult": "net.minecraft.world.inventory.ResultContainer",
    "net.minecraft.inventory.ClickType": "net.minecraft.world.inventory.ClickType",
    "net.minecraft.inventory.IContainerListener": "net.minecraft.world.inventory.ContainerListener",
    "net.minecraft.inventory.InventoryHelper": "net.minecraft.world.Containers",
    "net.minecraft.item.crafting.Ingredient": "net.minecraft.world.item.crafting.Ingredient",
    "net.minecraft.item.crafting.IRecipe": "net.minecraft.world.item.crafting.Recipe",
    "net.minecraft.item.ItemSword": "net.minecraft.world.item.SwordItem",
    "net.minecraft.item.ItemShears": "net.minecraft.world.item.ShearsItem",
    "net.minecraft.item.ItemDye": "net.minecraft.world.item.DyeItem",
    "net.minecraft.item.EnumDyeColor": "net.minecraft.world.item.DyeColor",
    "net.minecraft.potion.PotionUtils": "net.minecraft.world.item.alchemy.PotionUtils",
    "net.minecraft.potion.PotionType": "net.minecraft.world.item.alchemy.Potion",
    "net.minecraft.stats.StatList": "net.minecraft.stats.Stats",
    "net.minecraft.tileentity.TileEntityFurnace": "net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity",
    "net.minecraft.tileentity.TileEntitySign": "net.minecraft.world.level.block.entity.SignBlockEntity",
    "net.minecraft.tileentity.TileEntitySkull": "net.minecraft.world.level.block.entity.SkullBlockEntity",
    "net.minecraft.tileentity.TileEntityMobSpawner": "net.minecraft.world.level.block.entity.SpawnerBlockEntity",
    "net.minecraft.tileentity.TileEntityBed": "net.minecraft.world.level.block.entity.BedBlockEntity",
    "net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher": "net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher",
    "net.minecraft.client.renderer.vertex.DefaultVertexFormats": "com.mojang.blaze3d.vertex.DefaultVertexFormat",
    "net.minecraft.client.renderer.block.model.BakedQuad": "net.minecraft.client.renderer.block.model.BakedQuad",
    "net.minecraft.client.renderer.texture.TextureMap": "net.minecraft.client.renderer.texture.TextureAtlas",
    "net.minecraft.client.renderer.texture.SimpleTexture": "net.minecraft.client.renderer.texture.SimpleTexture",
    "net.minecraft.client.renderer.color.ItemColors": "net.minecraft.client.color.item.ItemColors",
    "net.minecraft.client.resources.IResourceManager": "net.minecraft.server.packs.resources.ResourceManager",
    "net.minecraft.client.audio.PositionedSoundRecord": "net.minecraft.client.resources.sounds.SimpleSoundInstance",
    "net.minecraft.util.EnumParticleTypes": "net.minecraft.core.particles.ParticleTypes",
    "net.minecraft.util.EnumBlockRenderType": "net.minecraft.world.level.block.RenderShape",
    "net.minecraft.world.storage.loot.LootTable": "net.minecraft.world.level.storage.loot.LootTable",
    "net.minecraft.world.storage.loot.LootTableList": "net.minecraft.world.level.storage.loot.BuiltInLootTables",
    "net.minecraft.world.chunk.IChunkProvider": "net.minecraft.world.level.chunk.ChunkSource",
    "net.minecraft.world.gen.IChunkGenerator": "net.minecraft.world.level.chunk.ChunkGenerator",
    # Forge package moves/removals with direct equivalents.
    "net.minecraftforge.items.CapabilityItemHandler": "net.minecraftforge.common.capabilities.ForgeCapabilities",
    "net.minecraftforge.fml.common.Loader": "net.minecraftforge.fml.ModList",
    "net.minecraftforge.fml.relauncher.ReflectionHelper": "net.minecraftforge.fml.util.ObfuscationReflectionHelper",
    "net.minecraftforge.fml.common.ObfuscationReflectionHelper": "net.minecraftforge.fml.util.ObfuscationReflectionHelper",
    "net.minecraftforge.fml.common.gameevent.TickEvent": "net.minecraftforge.event.TickEvent",
    "net.minecraftforge.fml.common.gameevent.InputEvent": "net.minecraftforge.client.event.InputEvent",
    "net.minecraftforge.event.world.ChunkWatchEvent": "net.minecraftforge.event.level.ChunkWatchEvent",
    "net.minecraftforge.oredict.OreDictionary": "net.shadowmage.ancientwarfare.core.compat.LegacyOreDictionary",
}


TOKENS = {
    "Material": "LegacyMaterial",
    "CreativeTabs": "LegacyCreativeTab",
    "TextComponentTranslation": "Component",
    "TextComponentString": "Component",
    "ITextComponent": "Component",
    "TextFormatting": "ChatFormatting",
    "JsonUtils": "GsonHelper",
    "SharedMonsterAttributes": "Attributes",
    "IRangedAttackMob": "RangedAttackMob",
    "INpc": "Npc",
    "EntityHorse": "Horse",
    "EntityPig": "Pig",
    "IMob": "Enemy",
    "EntityArrow": "AbstractArrow",
    "EntityPotion": "ThrownPotion",
    "EntityList": "BuiltInRegistries",
    "EntityAISwimming": "FloatGoal",
    "EntityAIWatchClosest2": "LookAtPlayerGoal",
    "EntityAIWatchClosest": "LookAtPlayerGoal",
    "EntityAINearestAttackableTarget": "NearestAttackableTargetGoal",
    "EntityAITarget": "TargetGoal",
    "EntityAIHurtByTarget": "HurtByTargetGoal",
    "RandomPositionGenerator": "DefaultRandomPos",
    "PathNavigateGround": "GroundPathNavigation",
    "WalkNodeProcessor": "WalkNodeEvaluator",
    "PathNodeType": "BlockPathTypes",
    "PathPoint": "Node",
    "InventoryCrafting": "CraftingContainer",
    "InventoryCraftResult": "ResultContainer",
    "IContainerListener": "ContainerListener",
    "InventoryHelper": "Containers",
    "IRecipe": "Recipe",
    "ItemSword": "SwordItem",
    "ItemShears": "ShearsItem",
    "ItemDye": "DyeItem",
    "EnumDyeColor": "DyeColor",
    "PotionType": "Potion",
    "StatList": "Stats",
    "TileEntityFurnace": "AbstractFurnaceBlockEntity",
    "TileEntitySign": "SignBlockEntity",
    "TileEntitySkull": "SkullBlockEntity",
    "TileEntityMobSpawner": "SpawnerBlockEntity",
    "TileEntityBed": "BedBlockEntity",
    "TileEntityRendererDispatcher": "BlockEntityRenderDispatcher",
    "DefaultVertexFormats": "DefaultVertexFormat",
    "TextureMap": "TextureAtlas",
    "ItemColors": "ItemColors",
    "IResourceManager": "ResourceManager",
    "PositionedSoundRecord": "SimpleSoundInstance",
    "EnumParticleTypes": "ParticleTypes",
    "EnumBlockRenderType": "RenderShape",
    "LootTableList": "BuiltInLootTables",
    "IChunkProvider": "ChunkSource",
    "IChunkGenerator": "ChunkGenerator",
    "CapabilityItemHandler": "ForgeCapabilities",
    "Loader": "ModList",
    "ReflectionHelper": "ObfuscationReflectionHelper",
    "OreDictionary": "LegacyOreDictionary",
    "NBTTagCompound": "CompoundTag",
    "NBTTagList": "ListTag",
    "NBTTagString": "StringTag",
    "NBTTagInt": "IntTag",
    "NBTTagLong": "LongTag",
    "NBTTagByte": "ByteTag",
    "NBTTagShort": "ShortTag",
    "NBTTagFloat": "FloatTag",
    "NBTTagDouble": "DoubleTag",
    "NBTTagByteArray": "ByteArrayTag",
    "NBTTagIntArray": "IntArrayTag",
    "NBTBase": "Tag",
    "EnumFacing": "Direction",
    "EnumHand": "InteractionHand",
    "EnumActionResult": "InteractionResult",
    "ActionResult": "InteractionResultHolder",
    "EntityLivingBase": "LivingEntity",
    "EntityLiving": "Mob",
    "EntityPlayerMP": "ServerPlayer",
    "EntityPlayer": "Player",
    "EntityItem": "ItemEntity",
    "WorldServer": "ServerLevel",
    "World": "Level",
    "IBlockState": "BlockState",
    "TileEntity": "BlockEntity",
    "Item.ToolMaterial": "Tier",
    "ItemStack.EMPTY": "ItemStack.EMPTY",
}


METHODS = {
    ".getResourcePath()": ".getPath()",
    ".getResourceDomain()": ".getNamespace()",
    ".getFirst()": ".getA()",
    ".getSecond()": ".getB()",
    ".toLong()": ".asLong()",
    "BlockPos.fromLong(": "BlockPos.of(",
    ".up()": ".above()",
    ".down()": ".below()",
    ".up(": ".above(",
    ".down(": ".below(",
    ".rotateY()": ".getClockWise()",
    ".rotateYCCW()": ".getCounterClockWise()",
    ".getDefaultState()": ".defaultBlockState()",
    ".withProperty(": ".setValue(",
    ".getValue(": ".getValue(",
    ".getBlockState(": ".getBlockState(",
    ".getTileEntity(": ".getBlockEntity(",
    ".setBlockState(": ".setBlock(",
    ".isAirBlock(": ".isEmptyBlock(",
    ".isBlockLoaded(": ".hasChunkAt(",
    ".getEntityByID(": ".getEntity(",
    ".getEntityId()": ".getId()",
    ".getUniqueID()": ".getUUID()",
    ".getPositionVector()": ".position()",
    ".getEntityBoundingBox()": ".getBoundingBox()",
    ".isSneaking()": ".isShiftKeyDown()",
    ".isDead": ".isRemoved()",
    ".isRiding()": ".isPassenger()",
    ".getRidingEntity()": ".getVehicle()",
    ".dismountRidingEntity()": ".stopRiding()",
    ".isBeingRidden()": ".isVehicle()",
    ".getHeldItemMainhand()": ".getMainHandItem()",
    ".getHeldItemOffhand()": ".getOffhandItem()",
    ".getHeldItem(": ".getItemInHand(",
    ".setHeldItem(": ".setItemInHand(",
    ".isHandActive()": ".isUsingItem()",
    ".getActiveHand()": ".getUsedItemHand()",
    ".setActiveHand(": ".startUsingItem(",
    ".resetActiveHand()": ".stopUsingItem()",
    ".swingArm(": ".swing(",
    ".getRNG()": ".getRandom()",
    ".getNavigator()": ".getNavigation()",
    ".getMoveHelper()": ".getMoveControl()",
    ".getLookHelper()": ".getLookControl()",
    ".getEntitySenses()": ".getSensing()",
    ".getEntityAttribute(": ".getAttribute(",
    ".clearPath()": ".stop()",
    ".noPath()": ".isDone()",
    ".tryMoveToXYZ(": ".moveTo(",
    ".tryMoveToEntityLiving(": ".moveTo(",
    ".getAttackTarget()": ".getTarget()",
    ".setAttackTarget(": ".setTarget(",
    ".getRevengeTarget()": ".getLastHurtByMob()",
    ".setRevengeTarget(": ".setLastHurtByMob(",
    ".getLastAttackedEntity()": ".getLastHurtMob()",
    ".canEntityBeSeen(": ".getSensing().hasLineOfSight(",
    ".getSensing().canSee(": ".getSensing().hasLineOfSight(",
    ".getCurrentPathIndex()": ".getNextNodeIndex()",
    ".getCurrentPathLength()": ".getNodeCount()",
    ".getPathPointFromIndex(": ".getNode(",
    ".setCurrentPathLength(": ".truncateNodes(",
    ".addVector(": ".add(",
    ".getEntitiesWithinAABB(": ".getEntitiesOfClass(",
    ".getAIMoveSpeed()": ".getSpeed()",
    ".setAIMoveSpeed(": ".setSpeed(",
    ".getPathPriority(": ".getPathfindingMalus(",
    ".setPathPriority(": ".setPathfindingMalus(",
    ".getBlockPathWeight(": ".getWalkTargetValue(",
    ".getMaxFallHeight()": ".getMaxFallDistance()",
    ".isEntityAlive()": ".isAlive()",
    ".setFire(": ".setSecondsOnFire(",
    ".addSlotToContainer(": ".addSlot(",
    ".mergeItemStack(": ".moveItemStackTo(",
    ".detectAndSendChanges()": ".broadcastChanges()",
    ".getHasStack()": ".hasItem()",
    ".getStack()": ".getItem()",
    ".putStack(": ".set(",
    ".onTake(": ".onTake(",
    ".getSlotStackLimit()": ".getMaxStackSize()",
    ".getMetadata()": ".getDamageValue()",
    ".getItemDamage()": ".getDamageValue()",
    ".setItemDamage(": ".setDamageValue(",
    ".getMaxStackSize()": ".getMaxStackSize()",
    ".getTagCompound()": ".getTag()",
    ".hasTagCompound()": ".hasTag()",
    ".setTagCompound(": ".setTag(",
    ".getCompoundTag(": ".getCompound(",
    ".getTagList(": ".getList(",
    ".hasKey(": ".contains(",
    ".setString(": ".putString(",
    ".setInteger(": ".putInt(",
    ".setLong(": ".putLong(",
    ".setBoolean(": ".putBoolean(",
    ".setByte(": ".putByte(",
    ".setShort(": ".putShort(",
    ".setFloat(": ".putFloat(",
    ".setDouble(": ".putDouble(",
    ".getInteger(": ".getInt(",
    ".getBoolean(": ".getBoolean(",
    ".getKeySet()": ".getAllKeys()",
    ".tagCount()": ".size()",
    ".appendTag(": ".add(",
}


REGEX_REPLACEMENTS = (
    (
        re.compile(r"\bworld\.getBlockState\(([^;\n]+)\)\.getMaterial\(\)"),
        r"LegacyMaterial.of(world.getBlockState(\1))",
    ),
    (
        re.compile(r"\b([A-Za-z_$][\w$]*)\.defaultBlockState\(\)\.getMaterial\(\)"),
        r"LegacyMaterial.of(\1.defaultBlockState())",
    ),
    (
        re.compile(r"\bgetBlock\(b\)\.get\(\)\.defaultBlockState\(\)\.getMaterial\(\)"),
        r"LegacyMaterial.of(getBlock(b).get().defaultBlockState())",
    ),
    (
        re.compile(r"\b(state|stateBelow|doorState|stateLeft|stateRight|st|sl)\.getMaterial\(\)"),
        r"LegacyMaterial.of(\1)",
    ),
    (re.compile(r"\bnew\s+ResourceLocation\(([^,()]+)\)"), r"new ResourceLocation(\1)"),
    (re.compile(r"\bEnumFacing\.VALUES\b"), "Direction.values()"),
    (re.compile(r"\bToolMaterial\b"), "Tier"),
    (re.compile(r"\bEntityEquipmentSlot\b"), "EquipmentSlot"),
    (re.compile(r"\bNonNullList\.withSize\("), "NonNullList.withSize("),
)


def migrate_code(text: str) -> str:
    # Convert constructors before their imports/types are renamed to Component.
    text = re.sub(
        r"\bnew\s+TextComponentTranslation\s*\(",
        "Component.translatable(",
        text,
    )
    text = re.sub(
        r"\bnew\s+TextComponentString\s*\(",
        "Component.literal(",
        text,
    )
    for old, new in LITERALS.items():
        text = text.replace(old, new)
    for old, new in TOKENS.items():
        text = re.sub(rf"\b{re.escape(old)}\b", new, text)
    for old, new in METHODS.items():
        text = text.replace(old, new)
    for pattern, replacement in REGEX_REPLACEMENTS:
        text = pattern.sub(replacement, text)
    return text


def migrate(text: str) -> str:
    """Migrate Java code while leaving literals and comments byte-for-byte intact."""
    output: list[str] = []
    offset = 0
    for match in JAVA_NON_CODE.finditer(text):
        output.append(migrate_code(text[offset:match.start()]))
        output.append(match.group(0))
        offset = match.end()
    output.append(migrate_code(text[offset:]))
    return "".join(output)


def main() -> None:
    changed = 0
    for path in JAVA_ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8-sig")
        migrated = migrate(original)
        if migrated != original:
            path.write_text(migrated, encoding="utf-8", newline="\n")
            changed += 1
    print(f"Updated {changed} Java files")


if __name__ == "__main__":
    main()
