# CodeChickenLib 1.20.1 迁移说明

源码中有 47 个 Java 文件直接引用 CodeChickenLib。旧 CCL 几何数据可以尽量保留，但 1.12.2 的模型注册、扩展方块状态和立即模式渲染入口不能原样使用。

## 必须替换的边界

| 1.12.2 方式 | Forge/CCL 1.20.1 方向 |
|---|---|
| `TileEntitySpecialRenderer` | `BlockEntityRenderer<T>` |
| `GlStateManager` 固定管线 | `PoseStack` + `MultiBufferSource` + `RenderType` |
| `IModelState` / `TRSRTransformation` | CCL `PerspectiveModelState` / Mojang `Transformation` |
| 旧 `IItemRenderer#renderItem(ItemStack, TransformType)` | `renderItem(ItemStack, ItemDisplayContext, PoseStack, MultiBufferSource, packedLight, packedOverlay)` |
| `IExtendedBlockState` / unlisted property | `ModelData`、正常 BlockState 属性或自定义 baked model 数据 |
| `ModelRegistryHelper` / 旧 bakery 注册 | `ModelEvent.RegisterAdditional`、`ModelEvent.ModifyBakingResult`、`RegisterClientReloadListenersEvent` |

## 高优先级渲染器

- `structure/render/ProtectionFlagRenderer.java`
- `structure/render/StoneCoffinRenderer.java`
- `structure/render/WoodenCoffinRenderer.java`
- `structure/render/RenderItemAdvancedLootChest.java`
- `automation/render/*Renderer.java`
- `core/render/BaseBakery.java`
- `core/render/RotatableBlockRenderer.java`

## 保留原则

1. 保留 `CCModel` 顶点、法线、UV 与动画矩阵计算。
2. 把 GL 状态调用移到渲染层选择、PoseStack 变换和 VertexConsumer 输出。
3. 不把带动画的 CCL 模型静默替换成静态 JSON 模型。
4. 每个 BER 的渲染距离、剔除、光照和 overlay 必须在游戏内验证。

完整命中文件清单见 `port_audit_java.csv`，筛选 `category=codechickenlib`。
