const express = require('express');
const router = express.Router();

// POST /api/v1/media/upload-url
router.post('/upload-url', (req, res) => {
    const { contentType, fileSize } = req.body;

    if (!contentType || !fileSize) {
        return res.status(400).json({ error: 'Content type and file size are required.' });
    }

    if (fileSize > 50 * 1024 * 1024) {
        return res.status(400).json({ error: 'File size exceeds the 50MB limit.' });
    }

    // Return a fake pre-signed URL for direct S3/OSS upload
    res.json({
        data: {
            uploadUrl: `https://s3.aurelian-night.app/presigned-upload/${Date.now()}_video.mp4?signature=mock_sig`,
            mediaId: `med_${Date.now()}`,
            expiresIn: 3600 // 1 hour
        }
    });
});

// POST /api/v1/media/confirm
router.post('/confirm', (req, res) => {
    const { mediaId } = req.body;
    if (!mediaId) {
        return res.status(400).json({ error: 'Media ID is required.' });
    }

    // Trigger backend transcoding job
    res.json({
        data: {
            success: true,
            processing: true,
            message: "媒体文件已接收，正在进行后台高画质转码与安全审核。"
        }
    });
});

module.exports = router;
