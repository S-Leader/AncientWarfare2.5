# FTB Teams 1.20.1 兼容说明

已修改：

- `core/compat/ftb/FTBCompat.java`
- `core/compat/ftb/FTBTeamViewer.java`
- `core/owner/ScoreboardTeamViewer.java`

## API 映射

| 旧 FTB Lib | FTB Teams 1.20.1 |
|---|---|
| `Universe.get().getPlayer(UUID)` | `FTBTeamsAPI.api().getManager()` 后按 UUID 查询 |
| `ForgePlayer.getTeam()` | `TeamManager#getTeamForPlayerID(UUID)` |
| `ForgeTeam#getUID()` | `Team#getTeamId()` |
| 队友判断 | `TeamManager#arePlayersInSameTeam(UUID, UUID)` |
| `ForgeTeam#isAlly` | 无直接等价；当前安全降级为同队，计分板联盟单独处理 |

第三方 API 被限制在 `FTBTeamViewer` 内，避免核心所有权代码直接依赖 FTB Teams 类型。
