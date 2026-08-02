# 本轮新增/重点修改文件

## 自动化工具

- `tools/port_1_12_to_1_20_1.py`：保守 Java API 首轮迁移与 CSV 报告。
- `tools/convert_lang_to_json.py`：旧 `.lang` 转 JSON。
- `tools/audit_port.py`：全量 Java/资源迁移审计。

## 依赖兼容

- `src/main/java/net/shadowmage/ancientwarfare/core/compat/ftb/FTBCompat.java`
- `src/main/java/net/shadowmage/ancientwarfare/core/compat/ftb/FTBTeamViewer.java`
- `src/main/java/net/shadowmage/ancientwarfare/core/owner/ScoreboardTeamViewer.java`
- `src/main/java/net/shadowmage/ancientwarfare/npc/compat/ebwizardry/WizardryReduxBridge.java`
- `src/main/java/net/shadowmage/ancientwarfare/npc/compat/ebwizardry/FactionAllyDesignation.java`
- `src/main/java/net/shadowmage/ancientwarfare/npc/compat/ebwizardry/EBWizardryCompat.java`

## 元数据/资源

- `src/main/resources/META-INF/mods.toml`
- `src/main/resources/pack.mcmeta`
- `src/main/resources/assets/ancientwarfare/lang/*.json`
- `src/main/resources/assets/ancientwarfare/registry/manual/zh_cn/torque.json`（修复原文件 JSON 字符串中的未转义制表符）

## 报告与说明

- `PORTING_STATUS.md`
- `migration/CODECHICKENLIB_1_20_1.md`
- `migration/FTB_TEAMS_1_20_1.md`
- `migration/WIZARDRY_REDUX_1_20_1.md`
- `migration/RESOURCE_MIGRATION.md`
- `migration/VALIDATION_CHECKLIST.md`
- `migration/mechanical_changes.csv`
- `migration/unresolved_legacy_imports.csv`
- `migration/dependency_usage.csv`
- `migration/port_audit_summary.json`
- `migration/port_audit_java.csv`
- `migration/port_audit_resources.csv`
