package com.laidekuai.message.controller;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.SecurityUtils;
import com.laidekuai.message.dto.MessageDTO;
import com.laidekuai.message.dto.MessageRequest;
import com.laidekuai.message.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 留言控制器
 *
 * @author Laidekuai Team
 */
@Slf4j
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 发送留言
     * POST /api/messages
     */
    @PostMapping
    public Result<MessageDTO> sendMessage(@Valid @RequestBody MessageRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return messageService.sendMessage(request, userId);
    }

    /**
     * 删除留言
     * DELETE /api/messages/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable("id") Long messageId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return messageService.deleteMessage(messageId, userId);
    }

    /**
     * 商品留言列表
     * GET /api/messages/goods/{goodsId}
     */
    @GetMapping("/goods/{goodsId}")
    public Result<PageResult<MessageDTO>> listGoodsMessages(
            @PathVariable("goodsId") Long goodsId,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        return messageService.listGoodsMessages(goodsId, page, size);
    }
}
