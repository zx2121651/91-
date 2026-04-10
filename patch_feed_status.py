import re

file_path = "Aurelian-Backend/src/routes/feed.js"
with open(file_path, "r") as f:
    content = f.read()

# Change the default status of a newly published video to 'PENDING_REVIEW'
if "status: 'APPROVED' // 演示环境直接通过，跳过转码与人工机审" in content:
    content = content.replace(
        "status: 'APPROVED' // 演示环境直接通过，跳过转码与人工机审",
        "status: 'PENDING_REVIEW' // 视频上传后进入待审状态，管家团队将在后台审核后放入信息流"
    )

with open(file_path, "w") as f:
    f.write(content)
