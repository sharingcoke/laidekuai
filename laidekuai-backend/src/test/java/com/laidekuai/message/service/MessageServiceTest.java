package com.laidekuai.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.enums.GoodsStatus;
import com.laidekuai.goods.entity.Goods;
import com.laidekuai.goods.mapper.GoodsMapper;
import com.laidekuai.message.dto.MessageDTO;
import com.laidekuai.message.dto.MessageRequest;
import com.laidekuai.message.entity.Message;
import com.laidekuai.message.entity.MessageReply;
import com.laidekuai.message.mapper.MessageMapper;
import com.laidekuai.message.mapper.MessageReplyMapper;
import com.laidekuai.message.service.impl.MessageServiceImpl;
import com.laidekuai.user.entity.User;
import com.laidekuai.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private MessageReplyMapper messageReplyMapper;

    @Mock
    private GoodsMapper goodsMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    void sendMessage_GoodsNotApproved_ReturnsStatusError() {
        Goods goods = new Goods();
        goods.setId(1L);
        goods.setStatus(GoodsStatus.DRAFT);
        goods.setDeleted(0);

        when(goodsMapper.selectById(1L)).thenReturn(goods);

        MessageRequest request = new MessageRequest();
        request.setGoodsId(1L);
        request.setContent("hi");

        Result<MessageDTO> result = messageService.sendMessage(request, 10L);

        assertEquals(ErrorCode.GOODS_STATUS_ERROR.getCode(), result.getCode());
        verify(messageMapper, never()).insert(any(Message.class));
    }

    @Test
    void listGoodsMessages_UsesJoinResult() {
        Message message = new Message();
        message.setId(1L);
        message.setGoodsId(2L);
        message.setUserId(10L);
        message.setStatus("visible");
        message.setIsPurchased(1);
        message.setCreatedAt(LocalDateTime.now());

        Page<Message> page = new Page<>(1, 10);
        page.setRecords(List.of(message));
        page.setTotal(1);

        when(messageMapper.selectByGoodsId(any(Page.class), eq(2L), eq(true))).thenReturn(page);
        when(messageReplyMapper.selectByMessageId(1L)).thenReturn(List.of());
        when(messageMapper.selectReplies(1L)).thenReturn(List.of());

        User user = new User();
        user.setId(10L);
        user.setNickName("Tom");
        when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(user));

        Result<PageResult<MessageDTO>> result = messageService.listGoodsMessages(2L, 1L, 10L, true);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().getRecords().size());
        assertEquals(1, result.getData().getRecords().get(0).getIsPurchased());
    }

    @Test
    void replyMessage_MessageHidden_ReturnsError() {
        Message message = new Message();
        message.setId(5L);
        message.setStatus("hidden");
        message.setDeleted(0);

        when(messageMapper.selectById(5L)).thenReturn(message);

        Result<MessageDTO> result = messageService.replyMessage(5L, "hello", 11L);

        assertEquals(ErrorCode.NOT_FOUND.getCode(), result.getCode());
        verify(messageReplyMapper, never()).insert(any(MessageReply.class));
    }

    @Test
    void hideMessage_Visible_Success() {
        Message message = new Message();
        message.setId(6L);
        message.setStatus("visible");

        when(messageMapper.selectById(6L)).thenReturn(message);
        when(messageMapper.updateById(any(Message.class))).thenReturn(1);

        Result<Void> result = messageService.hideMessage(6L, 99L);

        assertTrue(result.isSuccess());
    }
}
