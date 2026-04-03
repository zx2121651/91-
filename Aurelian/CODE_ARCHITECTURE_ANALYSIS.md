# Aurelian Night: 项目代码与架构深度分析报告

本项目致力于打造一款面向高净值人群（HNWI）的高端私密社交短视频 App（Aurelian Night）。目前项目由基于 Android/Jetpack Compose 的前端代码库 (`Aurelian`) 和基于 Node.js/Express 的后端代码库 (`Aurelian-Backend`) 组成。

以下是对当前项目代码和架构的深度解剖：

---

## 1. 前端架构 (Android / Jetpack Compose)

前端部分是目前投入精力最大、架构最成熟的模块，其核心目标是“沉浸式极简体验”与“极致播放性能”。

### 1.1 UI 与表现层 (Presentation Layer)
*   **技术栈**: 采用全量 Jetpack Compose 构建，抛弃了传统的 XML 布局。
*   **设计语言 (Aurelian Design System)**:
    *   高度定制的 `Color.kt` 和 `Theme.kt`，确立了以 `DeepBlack (#131313)` 为主背景，`Gold (#D4AF37)` 和 `Silver (#E2E2E2)` 为点缀的“暗黑奢华风”。
    *   在 `MainFeedScreen.kt` 中实现了彻底摒弃 Material Design 默认样式的极简全屏视频流（`VerticalPager`）。去除了底部 TabBar，隐藏了所有不必要的文字和控制器，操作按钮被设计得极小且半透明，辅以底部深色渐变遮罩，将视觉焦点 100% 留给了全屏视频内容。
    *   引入了防御性 UI 截断（`TextOverflow.Ellipsis`）和大量安全边距（Padding），确保极端长文本的 Mock 数据也不会破坏高级感排版或遮挡交互区域。
    *   针对冷启动无缓存状态，抛弃了廉价的 `CircularProgressIndicator`，定制了基于 `Alpha` 渐变的“金色呼吸文字”骨架屏（Breathing Skeleton）。

### 1.2 视频渲染与性能优化 (Media Processing Pipeline)
视频流体验是短视频 App 的生命线。当前代码库在这方面进行了极深度的工程化重构：
*   **ExoPlayer 池化防 OOM (`ExoPlayerPool.kt`)**: 针对 `VerticalPager` 快速滑动可能导致的海量播放器实例创建和硬件解码器耗尽问题，实现了一个全局单例池。严格限制最多只存活 3 个 `ExoPlayer` 实例（当前播放、上预加载、下预加载），通过 `DisposableEffect` 生命周期进行动态租借（`acquire`）和归还（`release`）。
*   **精准带宽管理的智能预加载 (`VideoPrefetcher.kt`)**: 摒弃了简单粗暴的下载。引入了 Media3 的 `CacheWriter`，配合协程并发，实现了极其克制的“N+1, N+2”预加载策略。每段视频仅精准预取前 2MB 数据（`PRELOAD_BYTES`），并在滑动跳过时立刻调用 `cancelPrefetch()` 中断协程，将宝贵的网络带宽完全倾斜给当前正在观看的视频。
*   **Z-Order 层级重构防黑闪 (Z-Order Tearing Fix)**: 针对 Compose 动画与 Android 原生 `SurfaceView` 混编时的闪烁痛点，彻底反转了动画层级。`AndroidView`（承载 `SurfaceView`）作为最底层永远静止挂载（`setZOrderMediaOverlay(false)`），转而让位于其上的高斯模糊/高清封面图（`AsyncImage`）在视频 `STATE_READY` 时执行 `FadeOut` (700ms) 淡出动画。以极低的性能开销实现了从封面到视频流如丝般顺滑的跨端无缝过渡（Crossfade）。

### 1.3 数据流与架构设计 (Data & Domain Layer)
*   **Cache-First 离线首屏直出 (`FeedRepository.kt`)**: 针对高端用户可能处于游艇、地下车库等弱网环境，舍弃了简单的网络直连。在 Repository 层引入了基于协程 `Flow` 的“缓存优先”策略。当打开应用时，会瞬间（<50ms）将内存/本地的旧数据推给 UI 渲染，后台静默发起网络请求，并在拿到新数据后再次 `emit` 刷新 UI。这彻底消除了“白屏等待”的低级感。
*   **ViewModel 解耦 (`MainFeedViewModel.kt`)**: 剥离了直接发起请求的逻辑，全面接管了 Repository 推送的冷/热数据流（`collectLatest`），负责维护 `FeedUiState`（Loading, Success, Error）并处理点赞（`likeUser`）和触发滑动的预加载钩子。
*   **网络对接 (`Network.kt` / `NetworkClient.kt`)**: 使用 Retrofit2 配合 Gson，已定义了极其丰富（甚至超过目前 UI 所需）的 API 接口，涵盖认证、个人资料、匹配、私信、活动（Events）、午夜盲盒（Masquerade）等。目前客户端已经成功将 `BASE_URL` 指向本地 3000 端口，打通了端到端联调。

---

## 2. 后端架构 (Node.js / Express)

目前位于 `Aurelian-Backend` 目录下的后端代码处于一个相对初级的 MVP (最小可行性产品) 状态。

### 2.1 框架与中间件
*   **技术栈**: 采用 Node.js + Express.js 构建。
*   **安全与中间件**: 引入了 `helmet` (设置安全的 HTTP Headers)、`cors` (跨域资源共享) 和 `morgan` (HTTP 请求日志)。

### 2.2 路由与模块划分 (`server.js`)
后端的路由结构设计得非常宏大，与客户端的 `Network.kt` 高度契合，暗示了极高的业务复杂度：
*   `/api/v1/auth`: 登录、验证邀请码。
*   `/api/v1/feed`: 获取短视频流（目前已与客户端打通）。
*   `/api/v1/profile`: 个人资料与资产认证。
*   `/api/v1/matches` & `/api/v1/interactions`: 社交匹配、点赞/滑过。
*   `/api/v1/events`: 线下高定活动 RSVP。
*   `/api/v1/messages` & `/api/v1/invitations`: 私信与邀约。
*   `/api/v1/masquerade`: 匿名高端局玩法。
*   `/api/v1/media`: 视频/图片上传凭证获取。

### 2.3 当前缺陷与风险
正如上文多次强调的，后端目前虽然搭建了骨架并能响应客户端的 Feed 请求，但缺乏极其关键的“企业级”甚至“金融级”的深度支撑：
1.  **缺乏 JWT 鉴权隔离**: 接口是裸奔的，没有实现零信任机制，极易遭到越权访问（BOLA）和敏感数据爬虫攻击。
2.  **缺乏分布式与缓存层**: 面对 4K 高清视频分发和突发的社交高并发，简单的 Node.js 单实例将迅速崩溃。必须引入 Redis 缓存社交关系图谱，并强制客户端直传云存储（S3）加触发云转码。
3.  **缺乏长连接通信**: `messages` 路由目前还是 HTTP 的。必须升级为 WebSocket / Socket.io 网关架构，才能支撑高净值用户的即时通讯。

---

## 3. 架构总结与下一步方向 (Conclusion & Next Steps)

**总结**：Aurelian Night 的 Android 客户端在**UI/UX 质感**和**视频底层播放性能**（池化、精确预载、Z-Order 防闪、Cache-First）上已经具备了顶级大厂的商用级水准，完全能够支撑起“高端、极简、沉浸”的产品定位。但业务功能链条严重断裂，绝大部分复杂页面（私聊、主页、活动）依然是空白壳子。同时，Node.js 后端仅仅是一个能跑通联调的极简毛坯房。

**推荐开发优先级**：
1.  **闭环最高优（护城河）**：在 Android 端实现 `LoginScreen`（强制邀请码注册），在 Node 后端实现 JWT 签发与路由鉴权拦截。没有门槛，一切高端定位都是空谈。
2.  **社交核心（闭环）**：实现个人黑卡主页（`ProfileScreen`）以及从视频流点击进入主页的共享元素转场动画（Shared Element Transition），补齐社交了解的最后一块拼图。
3.  **通信基础设施**：将后端改造支持 WebSocket，并跑通客户端的点对点私聊机制。
