package com.laidekuai.message.controller;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.SecurityUtils;
import com.laidekuai.message.dto.MessageDTO;
import com.laidekuai.message.dto.MessageReplyRequest;
import com.laidekuai.message.dto.MessageRequest;
import com.laidekuai.message.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 留言控制器
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 发送留言
     * POST /api/messages
     */
    @PostMapping("/messages")
    public Result<MessageDTO> sendMessage(@Valid @RequestBody MessageRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return messageService.sendMessage(request, userId);
    }

    /**
     * 回复留言
     * POST /api/messages/{id}/replies
     */
    @PostMapping("/messages/{id}/replies")
    public Result<MessageDTO> replyMessage(@PathVariable("id") Long messageId,
                                           @Valid @RequestBody MessageReplyRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return messageService.replyMessage(messageId, request.getContent(), userId);
    }

    /**
     * 删除留言（用户）
     * DELETE /api/messages/{id}
     */
    @DeleteMapping("/messages/{id}")
    public Result<Void> deleteMessage(@PathVariable("id") Long messageId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return messageService.deleteMessage(messageId, userId);
    }

    /**
     * 商品留言列表（主路由）
     * GET /api/goods/{goodsId}/messages
     */
    @GetMapping("/goods/{goodsId}/messages")
    public Result<PageResult<MessageDTO>> listGoodsMessages(
            @PathVariable("goodsId") Long goodsId,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "purchased_only", required = false) Boolean purchasedOnly,
            @RequestParam(value = "is_purchased", required = false) Boolean isPurchased) {
        Boolean effectivePurchasedOnly = purchasedOnly != null ? purchasedOnly : isPurchased;
        return messageService.listGoodsMessages(goodsId, page, size, effectivePurchasedOnly);
    }

    /**
     * 商品留言列表（兼容旧路由，deprecated）
     * GET /api/messages/goods/{goodsId}
     */
    @Deprecated
    @GetMapping("/messages/goods/{goodsId}")
    public Result<PageResult<MessageDTO>> listGoodsMessagesCompat(
            @PathVariable("goodsId") Long goodsId,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "purchased_only", required = false) Boolean purchasedOnly,
            @RequestParam(value = "is_purchased", required = false) Boolean isPurchased) {
        Boolean effectivePurchasedOnly = purchasedOnly != null ? purchasedOnly : isPurchased;
        return messageService.listGoodsMessages(goodsId, page, size, effectivePurchasedOnly);
    }
}
