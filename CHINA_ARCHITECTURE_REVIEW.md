# Aurelian 项目代码与架构复盘（中国本地场景）

> 目标：基于当前仓库代码，重新分析前后端架构、工程成熟度与核心问题，并且重点评估“面向中国本地使用”的适配风险。

## 1. 项目当前形态（结论先行）

这个仓库本质上是一个**前后端同仓的产品原型（MVP）**：

- `Aurelian/`：Android App（Compose + Retrofit + Media3）
- `Aurelian-Backend/`：Node.js + Express mock API

它已经具备“发现/匹配/私信/活动/速约”等产品壳，但**离中国本地可上线产品**仍有较大距离，尤其在合规、安全、稳定性、可运维性方面。

---

## 2. 代码与架构拆解

### 2.1 后端（Express）

后端入口在 `server.js`，典型中间件 + 路由聚合方式：

- 安全与日志：`helmet`、`cors`、`morgan`
- 路由前缀：`/api/v1/*`
- 业务模块：auth/feed/profile/matches/events/messages/referrals/media/hookups 等

这种分模块写法对 MVP 友好，但目前仍是**路由层直出 mock 数据**，缺少：

- service/use-case 层
- repository/DAO 层
- 统一参数校验与错误码体系
- 持久化（MySQL/PostgreSQL/Redis）

### 2.2 Android 客户端

客户端在 `AurelianApp.kt` 使用 Compose Navigation + BottomBar，主流程比较完整：

- Discover / Matches / Messages / Hookups / Events / Profile

网络层集中在 `Network.kt` 单文件，包含大量 data class + Retrofit 接口定义。优点是开发快，缺点是：

- 领域耦合严重（Auth、Feed、Messages、Media、Hookups 全混在一起）
- 缺 OkHttp 统一拦截器（鉴权、签名、重试、灰度头）
- Base URL 写死模拟器地址（`10.0.2.2`）

### 2.3 速约模块（Hookups）现状

这轮新增的 hookups 具备了比初版更完整的功能链路：

- 后端支持筛选（city/intent）、分页（cursor/limit）
- 支持发起邀约 request，含 basic 校验
- 支持 request 状态查询
- 前端支持筛选 chip、分页加载、状态展示、轮询

从“交互演示”角度可用，但核心仍是内存数据 + mock 状态机。

---

## 3. 中国本地使用下的重点问题

### 3.1 合规风险（最高优先级）

如果定位中国本地产品，必须优先考虑以下问题：

1. **账号实名与未成年人保护**
   - 当前代码里没有实名认证流程，也没有年龄分级与未成年人保护逻辑。
   - 对社交/约会产品而言，这是高风险缺口。

2. **内容审核体系缺失**
   - 图片/视频/文本都没有接入审核（涉黄涉暴、低俗、违法内容）。
   - 国内上线通常需要“机审 + 人审 + 复审流转 + 申诉”。

3. **隐私与数据处理合规**
   - 需要满足 PIPL（个人信息保护法）相关要求：最小化收集、明示同意、删除导出、用途边界。
   - 当前后端没有看到用户数据生命周期治理能力。

4. **高风险功能的风控闭环不足**
   - 速约/邀约场景天然需要反骚扰、黑名单、举报、拉黑、风控评分。
   - 当前只有最基础 request 接口，不足以应对真实生态。

### 3.2 架构稳定性问题

1. **后端无持久化**
   - hooks 请求状态在内存 Map，重启即丢；无法做追溯、审计、风控。

2. **接口契约仍有历史不一致问题**
   - `invitations` 路由挂载与子路径定义存在重复前缀风险（文档与客户端易错位）。

3. **错误处理不标准化**
   - 目前多为临时 `res.status(...).json({ error: ... })`，缺少统一错误码字典与可观测字段（requestId、traceId、bizCode）。

4. **实时消息能力不足**
   - 私信仍是请求式 mock，缺少 WebSocket 长连接与送达/已读一致性设计。

### 3.3 工程可维护性问题

1. `Network.kt` 过于肥大，新增模块会不断加速膨胀。
2. ViewModel 层中存在直接调用 Retrofit 的模式，复用与测试都不友好。
3. 业务文案中中英混合、城市硬编码（Shanghai/Beijing/Shenzhen），本地化体验较粗糙。

---

## 4. 面向中国本地落地的建议路线

### Phase A（1~2 周）：先补“上线红线”

- 增加统一鉴权中间件与用户会话体系（JWT + refresh + 设备指纹）。
- 接入基础内容安全能力（文本 + 图片），所有 UGC 上链路审核状态。
- 引入举报/拉黑/风控名单表结构。

### Phase B（2~4 周）：把后端从 mock 变服务

- 拆分 controller/service/repository。
- 增加 MySQL/PostgreSQL 持久化，Redis 做会话和热点缓存。
- 为 hookups/messages/invitations 引入标准状态机与审计日志。

### Phase C（并行）：客户端架构治理

- 拆分 `Network.kt` 为按领域 package。
- 引入 Repository + UseCase 统一调用栈，ViewModel 不直连 API。
- 增加统一错误态组件、重试策略、埋点与崩溃归因。

### Phase D（上线前）：本地化与风控强化

- 全量中文化与合规文案（隐私协议、用户协议、青少年模式）。
- 城市/地理位置、时间格式、节假日活动推荐等本地化体验。
- 反骚扰策略（频率限制、陌生人邀请阈值、账号信用分）。

---

## 5. 一句话总结

当前项目可以作为“产品演示样板”，但如果目标是“中国本地真实运营”，优先级不应是继续加页面，而是先补齐**合规+风控+持久化+契约治理**四大底座。
