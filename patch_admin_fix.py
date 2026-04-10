import re

file_path = "Aurelian-Backend/src/routes/admin.js"
with open(file_path, "r") as f:
    content = f.read()

# Fix the transaction block issue where `request` and `user` are accessed outside the `try` block or let variables scope.
new_code = """
        const { action, reason, membershipTier } = value;

        // 1. 查找此审核记录
        const request = await VerificationRequest.findById(id).session(session);
        if (!request) {
            await session.abortTransaction();
            return res.status(404).json({ error: '找不到该条认证记录。' });
        }

        if (request.status !== 'PENDING') {
            await session.abortTransaction();
            return res.status(400).json({ error: `该记录已被处理，当前状态: \\${request.status}` });
        }

        // 2. 根据管理员的动作更新 Request 与 User
        const newStatus = action === 'APPROVE' ? 'APPROVED' : 'REJECTED';
        request.status = newStatus;
        request.rejectReason = action === 'REJECT' ? reason : null;
        request.reviewedBy = req.user.id;
        request.reviewedAt = new Date();

        await request.save({ session });

        // 找到申请用户
        let user = await User.findById(request.userId).session(session);
        if (user) {
            if (action === 'APPROVE') {
                user.status = 'ACTIVE';
                user.isVerified = true;
                if (membershipTier) {
                    user.membershipTier = membershipTier;
                }
            } else if (action === 'REJECT') {
                // 如果用户本身还在 PENDING 且资产被全盘否定，可以设为 REJECTED 或降级处理
                user.status = 'REJECTED';
            }
            await user.save({ session });
        }

        // 提交事务
        await session.commitTransaction();

        res.status(200).json({
            success: true,
            message: `成功\\${action === 'APPROVE' ? '通过' : '驳回'}该用户的审核请求。`,
            data: {
                requestId: request._id,
                userId: user?._id,
                newStatus: newStatus
            }
        });

    } catch (error) {
"""

if "const request =" in content and "let user =" not in content:
    # Use simple replace to fix the variable scoping
    content = content.replace("const user = await User.findById(request.userId).session(session);", "let user = await User.findById(request.userId).session(session);")
    content = content.replace("userId: user?._id,", "userId: user ? user._id : null,")

with open(file_path, "w") as f:
    f.write(content)
