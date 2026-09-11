# FakePlayer-CE 修复的Issues和合并的PRs/commits记录

## EndlessPixel/FakePlayer-CE
- [EndlessPixel/FakePlayer-CE Issue #3](https://github.com/EndlessPixel/FakePlayer-CE/issues/3) 修复 leaves 1.21.11 插件完全无法加载
- [EndlessPixel/FakePlayer-CE Issue #4](https://github.com/EndlessPixel/FakePlayer-CE/issues/4) 修复 26.2 Paper 服务端加载失败
- [EndlessPixel/FakePlayer-CE Issue #5](https://github.com/EndlessPixel/FakePlayer-CE/issues/5) 修复 leaf 1.21.11 执行指令时意外报错
- [EndlessPixel/FakePlayer-CE Issue #6](https://github.com/EndlessPixel/FakePlayer-CE/issues/6) 修复 build2 在 CommandAPI 11.2.0/12.0.0 下无法启用
- [EndlessPixel/FakePlayer-CE PR #7](https://github.com/EndlessPixel/FakePlayer-CE/pull/7) 修复 NMS 兼容性与插件生命周期稳定性（多版本加载/卸载崩溃）
- [EndlessPixel/FakePlayer-CE PR #8](https://github.com/EndlessPixel/FakePlayer-CE/pull/8) 新增特权复用真实玩家数据（名称被占用时可复用真实玩家数据生成假人）
- [EndlessPixel/FakePlayer-CE PR #10](https://github.com/EndlessPixel/FakePlayer-CE/pull/10) 修复假人使用/消耗物品后不自动补货（重写 `FakeplayerReplenishManager` 并增加补货前状态校验）

## tanyaofei/minecraft-fakeplayer
- [tanyaofei/minecraft-fakeplayer Issue #172](https://github.com/tanyaofei/minecraft-fakeplayer/issues/172) 修复占位符（如 `%fakeplayer_actions%`）无效：占位符 identifier 原取插件名导致前缀为 `fakeplayer-ce_`，改为固定 `fakeplayer`
- [tanyaofei/minecraft-fakeplayer Issue #178](https://github.com/tanyaofei/minecraft-fakeplayer/issues/178) 修复关闭无敌后仍无法造成伤害/击退
- [tanyaofei/minecraft-fakeplayer Issue #181](https://github.com/tanyaofei/minecraft-fakeplayer/issues/181) 修复无法关闭无敌模式及重载插件崩溃
- [tanyaofei/minecraft-fakeplayer PR #190](https://github.com/tanyaofei/minecraft-fakeplayer/pull/190) 修复切换维度后假人仍处于无敌状态（处理 `ClientboundRespawnPacket` 并调用 `player.hasChangedDimension()` 完成维度切换）
- [tanyaofei/minecraft-fakeplayer PR #196](https://github.com/tanyaofei/minecraft-fakeplayer/pull/196) 新增 1.21.11 NMS 模块支持（fakeplayer-v1_21_11），使插件可运行于 Minecraft 1.21.11
- [tanyaofei/minecraft-fakeplayer Issue #195](https://github.com/tanyaofei/minecraft-fakeplayer/issues/195) 修复工具不被补货（新增耐久阈值周期检查 `replenish.tools.durability-threshold`）
- [tanyaofei/minecraft-fakeplayer Issue #199](https://github.com/tanyaofei/minecraft-fakeplayer/issues/199) 修复无敌配置不生效（保存后立即推送至在线假人）
- [tanyaofei/minecraft-fakeplayer Issue #200](https://github.com/tanyaofei/minecraft-fakeplayer/issues/200) 修复 Mohist 加载崩溃（改用纯 NMS `LookUtils`）

## xiplugin/FakePlayer（同步修复）
- [xiplugin/FakePlayer commit 9d21b2c](https://github.com/xiplugin/FakePlayer/commit/9d21b2c8f410729c5d2e30ff71d3bf972c34dcbe) 修复动作过滤逻辑：`remains == -1`（无限次数）的动作被错误剔除，导致无限次动作不生效
- [xiplugin/FakePlayer commit 7162822](https://github.com/xiplugin/FakePlayer/commit/716282294dfbdd097ec831c75482ea1a6e5e9fc2) 修复查看假人背包：补回界面标题设置，并对 `openInventory` 返回 null 做保护避免空指针
- [xiplugin/FakePlayer commit 66f2af6](https://github.com/xiplugin/FakePlayer/commit/66f2af68cf176e731cb56fe15804f6482cbbab3e) 占位符增强：新增 `actions_translated` 占位符，显示经翻译器本地化的假人当前动作列表
- [xiplugin/FakePlayer commit d8ae21e](https://github.com/xiplugin/FakePlayer/commit/d8ae21e66618d9164df57e54bdcb068dca88f785) 重写低 TPS 假人限制：由低 TPS 时一次性全部移除改为按卡顿等级（laglevel）动态调整每位玩家假人上限并按召唤反序移除，卡顿恢复后逐步恢复上限；TPS 检测范围收窄到过去 1 分钟