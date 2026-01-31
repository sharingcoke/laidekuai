package com.laidekuai.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.goods.entity.Goods;
import com.laidekuai.goods.mapper.GoodsMapper;
import com.laidekuai.message.dto.MessageDTO;
import com.laidekuai.message.dto.MessageRequest;
import com.laidekuai.message.entity.Message;
import com.laidekuai.message.mapper.MessageMapper;
import com.laidekuai.message.service.MessageService;
import com.laidekuai.user.entity.User;
import com.laidekuai.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 留言服务实现类
 *
 * @author Laidekuai Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<MessageDTO> sendMessage(MessageRequest request, Long userId) {
        log.info("用户 {} 在商品 {} 发送留言", userId, request.getGoodsId());

        // 校验商品存在
        Goods goods = goodsMapper.selectById(request.getGoodsId());
        if (goods == null || goods.getDeleted() == 1) {
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        // 如果是回复，校验父留言存在
        if (request.getParentId() != null) {
            Message parent = messageMapper.selectById(request.getParentId());
            if (parent == null || parent.getDeleted() == 1) {
                return Result.error(40701, "回复的留言不存在");
            }
        }

        // 创建留言
        Message message = new Message();
        message.setGoodsId(request.getGoodsId());
        message.setSenderId(userId);
        message.setParentId(request.getParentId());
        message.setContent(request.getContent());
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);

        log.info("留言发送成功, ID: {}", message.getId());

        User user = userMapper.selectById(userId);
        String name = user != null ? user.getNickName() : null;
        String avatar = user != null ? user.getAvatarUrl() : null;

        return Result.success(MessageDTO.fromMessage(message, name, avatar));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteMessage(Long messageId, Long userId) {
        log.info("用户 {} 删除留言 {}", userId, messageId);

        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getDeleted() == 1) {
            return Result.error(40702, "留言不存在");
        }
        // 只有作者可以删除
        if (!message.getSenderId().equals(userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }

        messageMapper.deleteById(messageId);
        log.info("留言删除成功");
        return Result.success();
    }

    @Override
    public Result<PageResult<MessageDTO>> listGoodsMessages(Long goodsId, Long page, Long size) {
        Page<Message> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getGoodsId, goodsId)
                .isNull(Message::getParentId)
                .eq(Message::getDeleted, 0)
                .orderByDesc(Message::getCreatedAt);

        Page<Message> result = messageMapper.selectPage(pageParam, wrapper);

        List<MessageDTO> dtos = result.getRecords().stream()
                .map(message -> {
                    User user = userMapper.selectById(message.getSenderId());
                    String name = user != null ? user.getNickName() : null;
                    String avatar = user != null ? user.getAvatarUrl() : null;
                    MessageDTO dto = MessageDTO.fromMessage(message, name, avatar);

                    // 加载回复
                    List<Message> replies = messageMapper.selectReplies(message.getId());
                    if (replies != null && !replies.isEmpty()) {
                        dto.setReplies(replies.stream()
                                .map(reply -> {
                                    User replyUser = userMapper.selectById(reply.getSenderId());
                                    String replyName = replyUser != null ? replyUser.getNickName() : null;
                                    String replyAvatar = replyUser != null ? replyUser.getAvatarUrl() : null;
                                    return MessageDTO.fromMessage(reply, replyName, replyAvatar);
                                })
                                .collect(Collectors.toList()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        PageResult<MessageDTO> pageResult = new PageResult<>();
        pageResult.setRecords(dtos);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());

        return Result.success(pageResult);
    }
}
