# Aurelian Night (奥勒留之夜) - 后端 API 接口规范设计

本文档汇总了驱动“Aurelian Night”这款高端私密交友（视频滑动流）应用所需的核心 RESTful API 接口。

所有接口默认需要在 Request Header 中携带 `Authorization: Bearer <JWT_TOKEN>`（公开接口除外）。
基础路径前缀建议为：`/api/v1`

---

## 1. 认证与高定门槛 (Auth & Gatekeeping)

| 方法 | 路径 | 功能描述 | 请求参数示例 | 返回值示例 (Data部分) |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/auth/login` | 手机号/邮箱登录或注册 | `{ "email": "elite@example.com", "code": "123456" }` | `{ "token": "ey...", "isNewUser": false }` |
| `POST` | `/auth/verify-invite` | 验证高定邀请码（新用户必须） | `{ "inviteCode": "AURE-X79M-VQ2P" }` | `{ "valid": true, "referrerId": "usr_998" }` |
| `POST` | `/auth/biometric` | 绑定/验证 Face ID 或指纹设备 | `{ "deviceId": "...", "signature": "..." }` | `{ "success": true }` |

---

## 2. 个人资料与资产验证 (Profile & Vetting)

| 方法 | 路径 | 功能描述 | 请求参数示例 | 返回值示例 |
| :--- | :--- | :--- | :--- | :--- |
| `GET`  | `/profile/me` | 获取当前用户的完整资料和会籍状态 | - | `{ "id": "usr_1", "name": "林静恩", "membership": "ROYAL", "isVerified": true }` |
| `PUT`  | `/profile/update` | 更新个人主页文案、标签、基础信息 | `{ "bio": "古典乐与现代主义建筑的鉴赏者..." }` | `{ "success": true }` |
| `PUT`  | `/profile/preferences` | 更新偏好设置（如隐身模式、全球漫游） | `{ "stealthMode": true, "minAge": 25 }` | `{ "success": true }` |
| `POST` | `/vetting/submit-assets`| 提交资产/学历证明材料供后台审核 | `{ "documentUrls": ["s3://doc1.pdf"] }` | `{ "status": "PENDING_REVIEW" }` |

---

## 3. 沉浸式视频流与匹配 (Video Feed & Matchmaking)

| 方法 | 路径 | 功能描述 | 请求参数示例 | 返回值示例 |
| :--- | :--- | :--- | :--- | :--- |
| `GET`  | `/feed/videos` | **核心**：获取滑动推荐页的用户视频流（分页拉取，便于 Android 端 ExoPlayer 预加载） | `?cursor=last_id&limit=10` | `[{ "userId": "usr_2", "videoUrl": "https://cdn.../video.mp4", "coverUrl": "...", "name": "苏婉" }]` |
| `POST` | `/interactions/like` | 右滑 / 点击“心动” | `{ "targetUserId": "usr_2" }` | `{ "matched": true, "matchId": "mtc_1" }` |
| `POST` | `/interactions/pass` | 左滑 / 点击“无感” | `{ "targetUserId": "usr_2" }` | `{ "success": true }` |
| `GET`  | `/matches` | 获取已互相心动的匹配列表 | `?page=1` | `[{ "matchId": "mtc_1", "user": {...} }]` |
| `GET`  | `/admirers` | 获取喜欢了我的列表（普通会员返回高斯模糊图，高级会员返回清晰图） | - | `[{ "userId": "usr_3", "isBlurred": true }]` |

---

## 4. 私信与高定邀约 (Messaging & Private Invitations)

| 方法 | 路径 | 功能描述 | 请求参数示例 | 返回值示例 |
| :--- | :--- | :--- | :--- | :--- |
| `GET`  | `/conversations` | 获取会话列表（含置顶的“私人礼宾部”官方号）| - | `[{ "convId": "cnv_1", "lastMessage": "...", "unreadCount": 1 }]` |
| `GET`  | `/conversations/{id}/messages`| 获取某会话的详细聊天记录 | `?limit=20` | `[{ "msgId": "msg_1", "type": "TEXT", "content": "..." }]` |
| `POST` | `/messages/send` | 发送普通文本消息（建议配合 WebSocket） | `{ "convId": "cnv_1", "content": "非常期待明天的画廊..." }` | `{ "msgId": "msg_2", "timestamp": 16999999 }` |
| `POST` | `/invitations/send` | **核心**：发送高定私人邀约（请柬卡片） | `{ "targetUserId": "usr_2", "type": "米其林晚宴", "location": "宝格丽酒店", "time": "11-18 19:30", "message": "..." }` | `{ "inviteId": "inv_1", "status": "SENT" }` |
| `POST` | `/invitations/{id}/respond`| 接收方处理邀约（接受/婉拒） | `{ "action": "ACCEPT" }` | `{ "status": "ACCEPTED" }` |

---

## 5. 私密沙龙与文化体验 (Events & The Gallery)

| 方法 | 路径 | 功能描述 | 请求参数示例 | 返回值示例 |
| :--- | :--- | :--- | :--- | :--- |
| `GET`  | `/events` | 获取平台举办的高端线下活动（如东方雅集、假面舞会） | `?type=UPCOMING` | `[{ "eventId": "evt_1", "title": "金秋假面舞会", "date": "10-31" }]` |
| `GET`  | `/events/{id}` | 获取活动详情、着装要求、场地说明等 | - | `{ "title": "...", "attireProtocol": "Black Tie" }` |
| `POST` | `/events/{id}/rsvp` | 提交活动的参与申请（需审核） | `{ "partySize": 1 }` | `{ "status": "REQUESTED" }` |

---

## 6. 午夜盲盒机制 (Midnight Masquerade)

| 方法 | 路径 | 功能描述 | 请求参数示例 | 返回值示例 |
| :--- | :--- | :--- | :--- | :--- |
| `GET`  | `/masquerade/status` | 获取盲盒活动状态及倒计时 | - | `{ "isOpen": true, "endTime": 17000000, "question": "您最偏爱的单一麦芽威士忌是？" }` |
| `POST` | `/masquerade/submit` | 提交灵魂拷问的答案以尝试盲盒匹配 | `{ "answer": "麦卡伦25年..." }` | `{ "status": "MATCHING_IN_PROGRESS" }` |

---

## 7. 内推机制 (Referrals)

| 方法 | 路径 | 功能描述 | 请求参数示例 | 返回值示例 |
| :--- | :--- | :--- | :--- | :--- |
| `GET`  | `/referrals/status` | 获取用户的专属高定邀请码及剩余名额 | - | `{ "inviteCode": "AURE-X79M-VQ2P", "remaining": 3 }` |

---

## 8. 视频/多媒体上传直传 (Media Upload)

*为了节省后端带宽，App 的视频和高清图片必须直传云端 (如 AWS S3/OSS)。*

| 方法 | 路径 | 功能描述 | 请求参数示例 | 返回值示例 |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/media/upload-url` | 客户端请求一个预签名的直传 URL | `{ "contentType": "video/mp4", "fileSize": 10485760 }` | `{ "uploadUrl": "https://s3.../presigned", "mediaId": "med_1" }` |
| `POST` | `/media/confirm` | 客户端传完 S3 后，通知后端开始转码和处理 | `{ "mediaId": "med_1" }` | `{ "success": true, "processing": true }` |
