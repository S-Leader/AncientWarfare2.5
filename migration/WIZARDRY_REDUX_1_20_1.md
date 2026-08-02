# Electroblob's Wizardry Redux 1.20.1 兼容说明

Redux 仍使用 `ebwizardry` 资源命名空间，但 Java 代码已改写到 `com.binaris.wizardry`。本工作区新增了窄边界：

- `npc/compat/ebwizardry/WizardryReduxBridge.java`
- `npc/compat/ebwizardry/FactionAllyDesignation.java`
- `npc/compat/ebwizardry/EBWizardryCompat.java`

已完成：

- 可选模组加载检测。
- Redux 伤害类型/直接实体来源识别。
- 移除友军伤害判断对旧 `IElementalDamage` 的直接依赖。
- 将序列化法术标识统一为 `ResourceLocation`。

仍需绑定到你实际使用的 Redux 0.8.5.1 Forge 开发 JAR：

- `npc/entity/faction/NpcFactionSpellcasterWizardry.java`
- `npc/compat/ebwizardry/ai/EntityAIAttackSpellImproved.java`
- `npc/container/ContainerNpcFactionSpellcasterWizardry.java`
- `npc/gui/GuiNpcFactionSpellcasterWizardry.java`

这些文件包含旧 `ISpellCaster`、`Spell`、`SpellModifiers`、`SpellCastEvent` 和专用网络包。Redux 为重写版且 API 仍在演进，不能通过简单改 import 保证语义正确。正确做法是从你 Gradle 解析出的 Redux JAR/Javadoc 中确认：

1. 法术 registry key 与查找入口；
2. NPC caster 接口或 capability/component；
3. cast context、modifier 和 cooldown API；
4. 连续法术的开始/tick/停止事件；
5. Redux 自带的客户端施法同步包是否公开，或是否需要 AW 自己同步动画。

在这些签名确认前，旧四文件保留并在审计表中明确标红，避免“编译通过但施法逻辑完全错误”的假兼容。
