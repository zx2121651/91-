const express = require('express');
const router = express.Router();
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

// 注入双层安全验证:
// 1. 必须有有效的 JWT。
// 2. 必须是 'ACTIVE'（已受邀/已验证）用户。
router.get('/videos', verifyToken, requireActiveStatus, (req, res) => {

    // 记录用户 ID，证明我们已从 JWT 中提取
    console.log(`[SECURE FEED] 为高级会员提供高端内容: ${req.user.id}`);

    // 高质量的 Mock 数据，替换之前的测试数据，提供更丰富、更符合高端社交场景的短视频列表
    const mockFeed = [
        {
            userId: "usr_elite_01",
            name: "李泽楷",
            bio: "香港 | 私募基金合伙人 | 当代艺术收藏家",
            location: "香港, 中国",
            videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            coverUrl: "https://example.com/cover1.jpg",
            isLiked: false
        },
        {
            userId: "usr_elite_02",
            name: "顾里",
            bio: "上海 | 独立设计师品牌主理人 | 热爱马术与黑皮诺",
            location: "上海, 中国",
            videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            coverUrl: "https://example.com/cover2.jpg",
            isLiked: true
        },
        {
            userId: "usr_elite_03",
            name: "楚子航",
            bio: "北京 | 科技公司创始人 | 极致的性能控",
            location: "北京, 中国",
            videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            coverUrl: "https://example.com/cover3.jpg",
            isLiked: false
        },
        {
            userId: "usr_elite_04",
            name: "苏芒",
            bio: "巴黎 | 时尚买手 | 穿梭于各大时装周",
            location: "巴黎, 法国",
            videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
            coverUrl: "https://example.com/cover4.jpg",
            isLiked: false
        },
        {
            userId: "usr_elite_05",
            name: "陆霆骁",
            bio: "深圳 | 游艇俱乐部 VIP | 享受海风与宁静",
            location: "深圳, 中国",
            videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            coverUrl: "https://example.com/cover5.jpg",
            isLiked: true
        }
    ];

    res.status(200).json({
        data: mockFeed,
        nextCursor: "bGFzdF92aWRlb19pZF8wMDU="
    });
});

// 新增：短视频发布接口（用于前端模拟视频上传发布流程）
router.post('/publish', verifyToken, requireActiveStatus, (req, res) => {
    const { title, bio, mediaId } = req.body;

    if (!mediaId) {
         return res.status(400).json({ error: '必须提供有效的媒体文件ID' });
    }

    console.log(`[FEED PUBLISH] 用户 ${req.user.id} 提交了新的短视频。媒体ID: ${mediaId}, 标题: ${title}`);

    // Mock: 假设视频发布成功，并加入到后台处理队列
    res.status(200).json({
        success: true,
        message: '您的精彩瞬间已发布，正在进行高清处理。稍后将展示在您的主页中。'
    });
});

module.exports = router;
