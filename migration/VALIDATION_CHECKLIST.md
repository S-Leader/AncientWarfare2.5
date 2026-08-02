# 1.20.1 验证清单

## 编译阶段

- [ ] 五个 `@Mod` 入口改为构造器 + 模组事件总线。
- [ ] 所有注册项改为 DeferredRegister，并解决模块间 RegistryObject 引用。
- [ ] 旧代理拆为 Dist.CLIENT 事件和通用初始化。
- [ ] 所有 SimpleImpl 网络包改为 SimpleChannel，并为每个包验证 encode/decode/handle 线程。
- [ ] 所有 Container/GUI 改为 MenuType/MenuScreens。
- [ ] 所有 EntityType、属性、SpawnPlacement 与 goal API 编译通过。
- [ ] 所有 BlockEntityType、能力和菜单提供器编译通过。
- [ ] 所有 CCL/BER/实体渲染器编译通过。
- [ ] 所有命令改为 Brigadier。

## 数据与服务端阶段

- [ ] Dedicated server 无客户端类加载崩溃。
- [ ] 所有 loot table、recipe、tag、structure、worldgen datapack 校验通过。
- [ ] 结构生成不越界、不递归卡死、区块保存后可重载。
- [ ] NPC 阵营、所有权、FTB Teams 与计分板关系正确。
- [ ] Wizardry Redux 未安装时不会触发类加载错误。
- [ ] Wizardry Redux 安装时施法、冷却、伤害归属和客户端特效同步正确。

## 客户端阶段

- [ ] 所有菜单能打开、同步和关闭。
- [ ] CCL 动画模型姿态、光照、overlay、剔除和物品展示变换正确。
- [ ] OBJ、实体模型、粒子、声音和语言资源正常。
- [ ] JEI/其他可选兼容未安装时不崩溃。

## 存档阶段

- [ ] 旧 metadata 方块/物品有明确迁移映射。
- [ ] TileEntity NBT → BlockEntity NBT 转换策略确定。
- [ ] 实体 UUID、所有者、阵营与路径数据可恢复。
- [ ] 旧世界必须先备份；未完成 DataFixer 前不得承诺直接加载 1.12 存档。
