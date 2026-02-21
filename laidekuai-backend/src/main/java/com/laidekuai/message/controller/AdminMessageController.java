package com.laidekuai.message.controller;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.SecurityUtils;
import com.laidekuai.message.dto.MessageDTO;
import com.laidekuai.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员留言治理
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminMessageController {

    private final MessageService messageService;

    /**
     * GET /api/admin/messages
     */
    @GetMapping("/admin/messages")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<MessageDTO>> listMessages(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        return messageService.listAdminMessages(status, page, size);
    }

    /**
     * GET /api/admin/message-replies
     */
    @GetMapping("/admin/message-replies")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<MessageDTO>> listReplies(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        return messageService.listAdminReplies(status, page, size);
    }

    /**
     * POST /api/admin/messages/{id}/hide
     */
    @PostMapping("/admin/messages/{id}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> hideMessage(@PathVariable("id") Long messageId) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return messageService.hideMessage(messageId, adminId);
    }

    /**
     * POST /api/admin/messages/{id}/delete
     */
    @PostMapping("/admin/messages/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteMessage(@PathVariable("id") Long messageId) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return messageService.deleteMessageByAdmin(messageId, adminId);
    }

    /**
     * POST /api/admin/message-replies/{id}/hide
     */
    @PostMapping("/admin/message-replies/{id}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> hideReply(@PathVariable("id") Long replyId) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return messageService.hideReply(replyId, adminId);
    }

    /**
     * POST /api/admin/message-replies/{id}/delete
     */
    @PostMapping("/admin/message-replies/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteReply(@PathVariable("id") Long replyId) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return messageService.deleteReplyByAdmin(replyId, adminId);
    }
}
