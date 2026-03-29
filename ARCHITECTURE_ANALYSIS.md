# 项目代码与架构分析（Aurelian）

## 1. 项目整体结构

仓库当前是一个**前后端同仓（monorepo-like）**结构：

- `Aurelian/`：Android 客户端（Kotlin + Jetpack Compose + Media3 + Retrofit）。
- `Aurelian-Backend/`：Node.js/Express 后端（REST API，当前以 mock/in-memory 数据为主）。
- 根目录有多份 patch / script 文件，更多像是开发过程脚本，不属于主业务运行路径。

这意味着当前项目更偏向“**可演示的端到端 MVP**”，而不是已经模块化、可独立部署的生产级多服务系统。

---

## 2. 后端架构分析（Aurelian-Backend）

### 2.1 启动与中间件

后端入口是 `server.js`，采用标准 Express 组织方式：

- `helmet`、`cors`、`express.json`、`morgan` 作为全局 middleware。
- 统一挂载 `/api/v1/*` 路由前缀。
- `/health` 健康检查和统一错误处理中间件。

这套骨架简洁清晰，适合快速扩展路由。

### 2.2 路由分层

`server.js` 把业务域拆成多个 route 文件（`auth/feed/profile/matches/events/interactions/messages/masquerade/referrals/media`），体现出“按领域切分”的组织方式。

优点：

- 领域边界清晰，避免把所有接口堆到单文件。
- 对客户端 API 分类友好（也与 Android `Network.kt` 分组一致）。

不足：

- 当前所有路由基本都是 mock 数据 + 无数据库。
- 没有 service/repository 层，路由层同时承担数据和业务逻辑。

### 2.3 数据与一致性现状

当前属于“演示数据驱动”：

- `feed.js` 返回固定卡片数据。
- `interactions.js` 中 `like` 使用随机概率模拟匹配。
- `messages.js` 直接返回静态会话与消息。

这种方式适合 UI 联调，但无法保证业务可重复性、可追踪性，也无法用于真实撮合/消息场景。

### 2.4 明显的接口契约偏差

在 `server.js` 里，`/api/v1/invitations` 被挂到了 `messagesRoutes`；但 `messages.js` 内实际定义的是 `router.post('/invitations/send')`。组合后会形成 `/api/v1/invitations/invitations/send`，与 Android 端声明的 `/api/v1/invitations/send` 不一致。

这属于**网关挂载路径与子路由定义重复前缀**的问题，真实联调会导致 404 或“文档可调、客户端不可调”的错位。

---

## 3. Android 客户端架构分析（Aurelian）

### 3.1 技术栈与依赖

客户端技术选型是当前主流 Android 方案：

- UI：Jetpack Compose + Material3 + Navigation Compose。
- 网络：Retrofit + Gson。
- 媒体：AndroidX Media3 ExoPlayer。
- 状态：ViewModel + Kotlin Flow。

可见项目目标明确：围绕“视频流社交”体验设计。

### 3.2 UI 与导航架构

`AurelianApp.kt` 使用 `NavHost` 管理页面，底部 `NavigationBar` 管 discover/matches/messages/events/profile 五个主 tab。

此外还有 login/chat/eventDetails/settings/masquerade/referral/subscription 等次级路由。结构上是典型：

- 主导航（底栏）
- 业务详情页（从主导航分支进入）

这使得信息架构比较直观，后续也可迁移到多 back stack 模式。

### 3.3 网络层设计

`Network.kt` 把所有 API 请求与数据模型集中在一个文件中，定义了：

- Request/Response data class
- `AurelianApiService` 接口
- `NetworkClient` Retrofit 单例

优点：

- 对小型项目很高效，便于快速迭代。

问题：

- 文件体量和职责已明显膨胀（认证、资料、匹配、消息、活动、媒体都混在一起）。
- 缺少认证拦截器（token 注入）与统一错误映射。
- `BASE_URL` 写死 `10.0.2.2:3000`，只适配本地模拟器开发，缺少环境切换策略。

### 3.4 状态管理与业务编排

目前是“**每个页面一个 ViewModel**”的轻量 MVVM 风格。例如：

- `MainFeedViewModel` -> `FeedRepository`
- `MessagesViewModel` -> 直接调用 `NetworkClient.apiService`

这里出现了一致性问题：

- Feed 已经引入 repository（有缓存优先策略）。
- 其他模块还在 ViewModel 直连 API。

结果是架构风格混用，后续扩展时维护成本会上升（重试、埋点、错误处理策略不统一）。

### 3.5 视频体验相关设计

项目有三个很关键的媒体模块：

- `ExoPlayerPool`：控制最多 3 个 player 实例，避免无限创建。
- `VideoCacheManager`（在 `MainActivity` 初始化）
- `VideoPrefetcher`：预拉首 2MB，降低首帧等待。

这体现了视频 Feed 产品应有的性能意识，且与 `ARCHITECTURE_RISKS.md` 中对解码与缓存风险的关注方向一致。

但也有潜在问题：

- 池满时“驱逐最旧实例”的策略较激进，可能造成边界场景下播放闪断。
- 预取取消依赖手工调用，若 UI 滑动事件未完整接入，会出现无效带宽消耗。

---

## 4. 文档与实现的一致性评估

项目内有两类高价值文档：

- `API_DOCUMENTATION.md`：定义理想 API 合约。
- `ARCHITECTURE_RISKS.md`：描述高端社交产品的性能/安全风险。

从现状看：

- **文档愿景很完整**（包括风控、上传转码、隐私安全）。
- **代码实现仍是 MVP/mock 阶段**（缺 JWT 鉴权链路、缺 DB、缺消息实时层、缺媒体处理流水线）。

换句话说，这个仓库最大的特点是：

> 目标架构是“高可靠高隐私视频社交平台”，当前实现是“可跑通基础交互的原型系统”。

---

## 5. 架构优点总结

1. **前后端领域模型基本对齐**：接口命名和业务模块（auth/feed/messages/events）大体一致。
2. **客户端视频能力提前布局**：有播放器池与预加载，而不是“先做 UI 再补性能”。
3. **导航与页面拆分清晰**：主流程（发现->匹配->私信->活动->我的）表达完整。
4. **风险意识强**：`ARCHITECTURE_RISKS.md` 已提前覆盖隐私、安全、转码、IM 扩展等关键问题。

---

## 6. 当前关键风险（按优先级）

### P0（会直接影响联调或上线）

- 路由路径重复前缀导致接口不可达风险（`/invitations`）。
- Android 与后端返回结构存在“靠宽松 model 映射凑合”的情况（未来极易踩坑）。
- 缺统一鉴权链路（文档要求 Bearer Token，但代码未建立完整机制）。

### P1（会影响可维护性和演进速度）

- `Network.kt` 超级文件化，职责耦合过高。
- ViewModel 调用风格不统一（有的走 Repository，有的直连 API）。
- 后端缺 service/data access 抽象层。

### P2（会影响规模化体验）

- 消息系统当前为请求式 mock，缺 WebSocket/推送与序列一致性机制。
- 媒体上传/转码/CDN 尚未落地，无法承载真实高码率视频生态。

---

## 7. 建议的演进路线（务实版本）

### 阶段 A：先把“契约一致性”修平

- 修复 invitations 路由挂载与子路由路径重复问题。
- 生成并共享 OpenAPI（单一真相源），前后端统一从 schema 生成 model。

### 阶段 B：把客户端架构从“可跑”变“可维护”

- 将 `Network.kt` 拆分为按领域文件（auth/feed/messages/events）。
- 强制 ViewModel 只依赖 Repository/UseCase，不直接碰 Retrofit。
- 增加 `OkHttp Interceptor` 实现 token 注入、traceId、统一错误码映射。

### 阶段 C：后端走向可生产

- 引入持久化层（PostgreSQL + Redis 起步）。
- 路由层下沉到 service 层，统一 DTO 校验（如 zod/joi）。
- 消息域拆出实时通道（WebSocket gateway + 消息持久化）。

### 阶段 D：兑现高端视频产品能力

- 完成上传直传 + 异步转码流水线。
- 在客户端加入网络质量自适应与多码率选择。
- 对高风险接口加入签名校验和设备完整性校验。

---

## 8. 一句话结论

这是一个**方向正确、产品意识强、但工程成熟度仍处于原型阶段**的项目。最该先做的不是“加更多页面”，而是“先把接口契约、分层边界和鉴权链路打牢”，否则规模一上来会迅速进入高维护成本区间。
