package net.shadowmage.ancientwarfare.structure.template;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.shadowmage.ancientwarfare.core.util.StringTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.api.*;
import net.shadowmage.ancientwarfare.structure.api.TemplateParsingException.TemplateRuleParsingException;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate.Version;
import net.shadowmage.ancientwarfare.structure.template.datafixes.DataFixManager;
import net.shadowmage.ancientwarfare.structure.template.datafixes.FixResult;
import net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.StructurePluginAutomation;
import net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.StructurePluginNpcs;
import net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.StructurePluginVanillaHandler;
import net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.StructurePluginVehicles;
import net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules.TemplateRuleBlockInventory;
import net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules.TemplateRuleBlockTile;
import net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules.TemplateRuleVanillaBlocks;
import net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.entityrules.TemplateRuleEntity;
import net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.entityrules.TemplateRuleEntityHanging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static net.shadowmage.ancientwarfare.structure.api.TemplateRule.JSON_PREFIX;

public class StructurePluginManager implements IStructurePluginRegister {
    private final List<StructureContentPlugin> loadedContentPlugins = new ArrayList<>();

    private final List<RuleHandler<IBlockDataMatcher, IBlockRuleCreator>> blockRuleHandlers = new ArrayList<>();
    private final List<RuleHandler<IEntityMatcher, IEntityRuleCreator>> entityRuleHandlers = new ArrayList<>();

    public static final StructurePluginManager INSTANCE = new StructurePluginManager();

    private StructurePluginManager() {
    }

    public void loadPlugins() {
        addPlugin(new StructurePluginVanillaHandler());

        if (ModList.get().isLoaded("ancientwarfarenpc")) {
            loadNpcPlugin();
        }
        if (ModList.get().isLoaded("ancientwarfarevehicle")) {
            loadVehiclePlugin();
        }
        if (ModList.get().isLoaded("ancientwarfareautomation")) {
            loadAutomationPlugin();
        }

        for (StructureContentPlugin plugin : loadedContentPlugins) {
            plugin.addHandledBlocks(this);
            plugin.addHandledEntities(this);
        }

        MinecraftForge.EVENT_BUS.post(new StructurePluginRegistrationEvent(this));

        registerBlockHandler(TemplateRuleBlockInventory.PLUGIN_NAME, (world, pos, state) -> state.hasBlockEntity() && WorldTools.hasItemHandler(world, pos),
                TemplateRuleBlockInventory::new, TemplateRuleBlockInventory::new);
        registerBlockHandler(TemplateRuleBlockTile.PLUGIN_NAME, (world, pos, state) -> state.hasBlockEntity() && !WorldTools.hasItemHandler(world, pos),
                TemplateRuleBlockTile::new, TemplateRuleBlockTile::new);
        registerBlockHandler(TemplateRuleVanillaBlocks.PLUGIN_NAME, (world, pos, state) -> !state.hasBlockEntity(),
                TemplateRuleVanillaBlocks::new, TemplateRuleVanillaBlocks::new);

        this.<HangingEntity>registerEntityHandler(TemplateRuleEntityHanging.PLUGIN_NAME, HangingEntity.class::isAssignableFrom, TemplateRuleEntityHanging::new, TemplateRuleEntityHanging::new);
        registerEntityHandler(TemplateRuleEntity.PLUGIN_NAME, clazz -> Entity.class.isAssignableFrom(clazz) && !Player.class.isAssignableFrom(clazz),
                TemplateRuleEntity::new, TemplateRuleEntity::new);
    }

    private void loadNpcPlugin() {
        addPlugin(new StructurePluginNpcs());
        AncientWarfareStructure.LOG.info("Loaded NPC Module Structure Plugin");
    }

    private void loadVehiclePlugin() {
        addPlugin(new StructurePluginVehicles());
        AncientWarfareStructure.LOG.info("Loaded Vehicle Module Structure Plugin");
    }

    private void loadAutomationPlugin() {
        addPlugin(new StructurePluginAutomation());
        AncientWarfareStructure.LOG.info("Loaded Automation Module Structure Plugin");
    }

    private void addPlugin(StructureContentPlugin plugin) {
        loadedContentPlugins.add(plugin);
    }

    public Optional<String> getPluginNameFor(Level world, BlockPos pos, BlockState state) {
        return getRuleHandler(world, pos, state).map(h -> h.pluginName);
    }

    Optional<TemplateRule> getRuleByName(String name) {
        Optional<TemplateRule> result = blockRuleHandlers.stream().filter(h -> h.pluginName.equals(name)).findFirst().map(h -> h.getRule.get());
        if (result.isPresent()) {
            return result;
        }
        return entityRuleHandlers.stream().filter(h -> h.pluginName.equals(name)).findFirst().map(h -> h.getRule.get());
    }

    public Optional<TemplateRuleBlock> getRuleForBlock(Level world, BlockState state, int turns, BlockPos pos) {
        Optional<IBlockRuleCreator> creator = getRuleHandler(world, pos, state).map(h -> h.ruleCreator);
        return creator.map(c -> c.create(world, pos, state, turns));
    }

    private Optional<RuleHandler<IBlockDataMatcher, IBlockRuleCreator>> getRuleHandler(Level world, BlockPos pos, BlockState state) {
        return blockRuleHandlers.stream().filter(h -> h.obj.matches(world, pos, state)).findFirst();
    }

    public Optional<TemplateRuleEntityBase> getRuleForEntity(Level world, Entity entity, int turns, int x, int y, int z) {
        return entityRuleHandlers.stream().filter(h -> h.obj.matches(entity.getClass())).findFirst().map(h -> h.ruleCreator)
                .map(c -> c.create(world, entity, turns, x, y, z));
    }

    public <T extends Entity> void registerEntityHandler(String pluginName, IEntityMatcher<T> entityMatcher, IEntityRuleCreator<T> creator, Supplier<TemplateRule> getParser) {
        entityRuleHandlers.add(new RuleHandler<>(entityMatcher, pluginName, creator, getParser));
    }

    public void registerEntityHandler(String pluginName, Class<? extends Entity> entityClass, IEntityRuleCreator creator, Supplier<TemplateRule> getParser) {
        entityRuleHandlers.add(new RuleHandler<>(entityClass::isAssignableFrom, pluginName, creator, getParser));
    }

    private void registerBlockHandler(String pluginName, IBlockDataMatcher blockMatcher, IBlockRuleCreator creator, Supplier<TemplateRule> getParser) {
        blockRuleHandlers.add(new RuleHandler<>(blockMatcher, pluginName, creator, getParser));
    }

    public void registerBlockHandler(String pluginName, Block block, IBlockRuleCreator creator, Supplier<TemplateRule> getParser) {
        registerBlockHandler(pluginName, (world, pos, state) -> state.getBlock() == block, creator, getParser);
    }

    public void registerPlugin(StructureContentPlugin plugin) {
        addPlugin(plugin);
    }

    public static <T extends TemplateRule> FixResult<T> getRule(Version version, List<String> ruleData, String ruleType) throws TemplateRuleParsingException {
        Iterator<String> it = ruleData.iterator();
        String name = null;
        int ruleNumber = -1;
        List<String> ruleDataPackage = new ArrayList<>();
        while (it.hasNext()) {
            String line = it.next();
            if (line.startsWith(ruleType + ":")) {
                continue;
            }
            if (line.startsWith(":end" + ruleType)) {
                break;
            }
            if (line.startsWith("plugin=")) {
                name = StringTools.safeParseString("=", line);
            }
            if (line.startsWith("number=")) {
                ruleNumber = StringTools.safeParseInt("=", line);
            }
            if (line.startsWith("data:")) {
                addData(it, ruleDataPackage);
            }
        }

        if (name == null || ruleNumber < 0 || ruleDataPackage.isEmpty()) {
            throw new TemplateRuleParsingException("Not enough data to create template rule.\n" + "name: " + name + "\n" + "number:" + ruleNumber + "\n" + "ruleDataPackage.size:" + ruleDataPackage.size() + "\n");
        }

        FixResult.Builder<T> resultBuilder = new FixResult.Builder<>();

        if (DataFixManager.getCurrentVersion().isGreaterThan(version)) {
            Tuple<String, List<String>> fixResult = resultBuilder.updateAndGetData(DataFixManager.fixRuleData(version, name, ruleDataPackage));
            name = fixResult.getA();
            ruleDataPackage = fixResult.getB();
        }

        Optional<TemplateRule> parser = INSTANCE.getRuleByName(name);
        if (!parser.isPresent()) {
            throw new TemplateRuleParsingException("Not enough data to create template rule.\n" + "Missing plugin for name: " + name + "\n" + "name: " + name + "\n" + "number:" + ruleNumber + "\n" + "ruleDataPackage.size:" + ruleDataPackage.size() + "\n");
        }

        TemplateRule rule = parser.get();
        rule.parseRule(readTag(ruleDataPackage));
        rule.ruleNumber = ruleNumber;

        T actualRule;
        try {
            //noinspection unchecked
            actualRule = (T) rule;
        } catch (ClassCastException e) {
            throw new TemplateRuleParsingException("Incorrect rule type is being returned\n");
        }

        return resultBuilder.build(actualRule);
    }

    private static void addData(Iterator<String> it, List<String> ruleDataPackage) {
        String line;
        while (it.hasNext()) {
            line = it.next();
            if (line.startsWith(":enddata")) {
                break;
            }
            ruleDataPackage.add(line);
        }
    }

    private static CompoundTag readTag(List<String> ruleData) throws TemplateRuleParsingException {
        for (String line : ruleData) {
            if (line.startsWith(JSON_PREFIX)) {
                try {
                    return TagParser.parseTag(line.substring(JSON_PREFIX.length()));
                } catch (CommandSyntaxException e) {
                    throw new TemplateRuleParsingException("Issue parsing CompoundTag from JSON: " + line, e);
                }
            }
        }
        return new CompoundTag();
    }

    private static class RuleHandler<T, U extends IRuleCreator> {
        private final T obj;
        private final String pluginName;
        private U ruleCreator;
        private Supplier<TemplateRule> getRule;

        private RuleHandler(T obj, String pluginName, U creator, Supplier<TemplateRule> getRule) {
            this.obj = obj;
            this.pluginName = pluginName;
            ruleCreator = creator;
            this.getRule = getRule;
        }
    }

    interface IRuleCreator {
    }

    public interface IBlockRuleCreator extends IRuleCreator {
        TemplateRuleBlock create(Level world, BlockPos pos, BlockState state, int turns);
    }

    public interface IEntityRuleCreator<T extends Entity> extends IRuleCreator {
        TemplateRuleEntityBase create(Level world, T entity, int turns, int x, int y, int z);
    }

    public interface IBlockDataMatcher {
        boolean matches(Level world, BlockPos pos, BlockState state);
    }

    public interface IEntityMatcher<T extends Entity> {
        boolean matches(Class<? extends T> entityClass);
    }
}
