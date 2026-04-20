import re

file_path = "Aurelian-Backend/src/routes/admin.js"
with open(file_path, "r") as f:
    content = f.read()

# Fix the RSVP confirm logic inside POST /events/rsvps/:rsvpId/review
old_confirm_logic = """        // 如果是把候补/待定转为确认，需要考虑容量 (在并发不高的情况下可以如此检查)
        if (action === 'CONFIRM') {
            const event = await Event.findById(rsvp.eventId).session(session);
            // 只有当之前没算进 enrolledCount 的时候才扣减
            if (rsvp.status !== 'PENDING' && rsvp.status !== 'WAITLISTED') {
                 // 其他奇怪状态暂不处理
            } else {
                 if (event.enrolledCount + rsvp.partySize > event.capacity) {
                     await session.abortTransaction();
                     return res.status(400).json({ error: '该沙龙的贵宾席位已满，无法通过更多申请。' });
                 }
                 // WAITLISTED 转为 CONFIRMED，需要增加 enrolledCount
                 event.enrolledCount += rsvp.partySize;
                 await event.save({ session });
            }
            rsvp.status = 'CONFIRMED';
        } else if (action === 'REJECT') {
            // 如果原本是 PENDING（已经占用了 enrolledCount），驳回时要释放名额
            if (rsvp.status === 'PENDING') {
                 const event = await Event.findById(rsvp.eventId).session(session);
                 event.enrolledCount = Math.max(0, event.enrolledCount - rsvp.partySize);
                 await event.save({ session });
            }
            rsvp.status = 'CANCELLED';
            rsvp.reason = reason || '由于场地受限，本次无法为您保留席位。';
        }

        await rsvp.save({ session });"""

new_confirm_logic = """        // 如果是确认动作
        if (action === 'CONFIRM') {
            const event = await Event.findById(rsvp.eventId).session(session);

            // 注意：在普通报名 (/events/:id/rsvp) 时，如果是 PENDING 状态，enrolledCount 已经增加了。
            // 所以从 PENDING 转 CONFIRMED 不需要再增加 enrolledCount。
            // 但如果从 WAITLISTED (之前名额满时存为候补) 转 CONFIRMED，则需要检查名额并增加 enrolledCount。
            if (rsvp.status === 'WAITLISTED') {
                 if (event.enrolledCount + rsvp.partySize > event.capacity) {
                     await session.abortTransaction();
                     return res.status(400).json({ error: '该沙龙的贵宾席位已满，无法通过更多申请。' });
                 }
                 event.enrolledCount += rsvp.partySize;
                 await event.save({ session });
            }
            rsvp.status = 'CONFIRMED';

        } else if (action === 'REJECT') {
            // 如果拒绝了 PENDING 或 CONFIRMED (占用名额的状态)，需要释放名额
            if (rsvp.status === 'PENDING' || rsvp.status === 'CONFIRMED') {
                 const event = await Event.findById(rsvp.eventId).session(session);
                 event.enrolledCount = Math.max(0, event.enrolledCount - rsvp.partySize);
                 await event.save({ session });
            }
            rsvp.status = 'CANCELLED';
            rsvp.reason = reason || '由于场地受限，本次无法为您保留席位。';
        }

        await rsvp.save({ session });"""

content = content.replace(old_confirm_logic, new_confirm_logic)

with open(file_path, "w") as f:
    f.write(content)
