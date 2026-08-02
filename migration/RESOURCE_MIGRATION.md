# 资源迁移说明

## 已自动转换

- 8 个 `assets/ancientwarfare/lang/*.lang` 已生成对应的小写 JSON。
- `META-INF/mods.toml` 与 `pack.mcmeta` 已创建。

## 禁止直接移动的旧资源

### Loot tables

旧表位于 `assets/ancientwarfare/loot_tables`，并包含 metadata、`set_data`、旧 pool/entry 字段。直接移动到 `data` 会导致数据包解析错误或掉落内容错误。

迁移时应先建立 metadata → 新注册 ID 映射，再转换 schema，最后移动到：

`data/ancientwarfare/loot_tables/...`

### Recipes

当前 `assets/.../recipes` 中部分内容由 Ancient Warfare 自定义加载器读取，不等同于现代原版 recipe JSON。必须在以下两种方式中选择：

- 实现现代 `RecipeSerializer` / `RecipeType`；或
- 保留自定义格式并使用 resource reload listener。

### Forge marker blockstates

含 `forge_marker` 的 71 个文件依赖 1.12 Forge 模型扩展。应按模型类型分别改为普通 variants/multipart、ModelData 自定义 baked model 或 CCL 动态渲染。

### Structures

结构 NBT 只有在结构加载器、处理器与资源 ID 全部迁移后才能迁往 `data/<namespace>/structures`。过早移动会让旧代码找不到资源。

逐文件清单见 `port_audit_resources.csv`。
