package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins;

import net.shadowmage.ancientwarfare.automation.init.AWAutomationBlocks;
import net.shadowmage.ancientwarfare.structure.api.StructureContentPlugin;
import net.shadowmage.ancientwarfare.structure.template.StructurePluginManager;
import net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules.TemplateRuleRotatable;
import net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules.TemplateRuleTorqueMultiblock;

public class StructurePluginAutomation implements StructureContentPlugin {
    @Override
    public void addHandledBlocks(StructurePluginManager manager) {
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.CROP_FARM.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.ANIMAL_FARM.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.FISH_FARM.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.TREE_FARM.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.QUARRY.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.WAREHOUSE_CONTROL.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);

        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.TORQUE_SHAFT_LIGHT.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.TORQUE_SHAFT_MEDIUM.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.TORQUE_SHAFT_HEAVY.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.TORQUE_JUNCTION_LIGHT.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.TORQUE_JUNCTION_MEDIUM.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.TORQUE_JUNCTION_HEAVY.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.TORQUE_DISTRIBUTOR_LIGHT.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.TORQUE_DISTRIBUTOR_MEDIUM.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.TORQUE_DISTRIBUTOR_HEAVY.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.FLYWHEEL_CONTROLLER_LIGHT.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.FLYWHEEL_CONTROLLER_MEDIUM.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.FLYWHEEL_CONTROLLER_HEAVY.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.STIRLING_GENERATOR.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.WATERWHEEL_GENERATOR.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.HAND_CRANKED_GENERATOR.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);
        manager.registerBlockHandler(TemplateRuleRotatable.PLUGIN_NAME, AWAutomationBlocks.WINDMILL_GENERATOR.get(), TemplateRuleRotatable::new, TemplateRuleRotatable::new);

        manager.registerBlockHandler(TemplateRuleTorqueMultiblock.PLUGIN_NAME, AWAutomationBlocks.FLYWHEEL_STORAGE_LIGHT.get(), TemplateRuleTorqueMultiblock::new, TemplateRuleTorqueMultiblock::new);
        manager.registerBlockHandler(TemplateRuleTorqueMultiblock.PLUGIN_NAME, AWAutomationBlocks.FLYWHEEL_STORAGE_MEDIUM.get(), TemplateRuleTorqueMultiblock::new, TemplateRuleTorqueMultiblock::new);
        manager.registerBlockHandler(TemplateRuleTorqueMultiblock.PLUGIN_NAME, AWAutomationBlocks.FLYWHEEL_STORAGE_HEAVY.get(), TemplateRuleTorqueMultiblock::new, TemplateRuleTorqueMultiblock::new);
        manager.registerBlockHandler(TemplateRuleTorqueMultiblock.PLUGIN_NAME, AWAutomationBlocks.WINDMILL_BLADE.get(), TemplateRuleTorqueMultiblock::new, TemplateRuleTorqueMultiblock::new);
    }

    @Override
    public void addHandledEntities(StructurePluginManager manager) {
        //noop, no entities in automation module
    }

}
