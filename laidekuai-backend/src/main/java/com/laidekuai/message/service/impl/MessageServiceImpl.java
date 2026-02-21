package com.laidekuai.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.laidekuai.message.service.MessageService;
import com.laidekuai.user.entity.User;
import com.laidekuai.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final MessageReplyMapper messageReplyMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<MessageDTO> sendMessage(MessageRequest request, Long userId) {
        log.info("User {} sends message for goods {}", userId, request.getGoodsId());

        Goods goods = goodsMapper.selectById(request.getGoodsId());
        if (goods == null || goods.getDeleted() == 1) {
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }
        if (goods.getStatus() != GoodsStatus.APPROVED) {
            return Result.error(ErrorCode.GOODS_STATUS_ERROR);
        }

        Message message = new Message();
        message.setGoodsId(request.getGoodsId());
        message.setUserId(userId);
        message.setSenderId(userId);
        message.setContent(request.getContent());
        message.setStatus("visible");
        message.setIsPurchased(0);
        LocalDateTime now = LocalDateTime.now();
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        message.setUpdatedBy(userId);
        messageMapper.insert(message);

        log.info("Message created ID: {}", message.getId());

        User user = userMapper.selectById(userId);
        String name = user != null ? user.getNickName() : null;
        String avatar = user != null ? user.getAvatarUrl() : null;

        return Result.success(MessageDTO.fromMessage(message, name, avatar));
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteMessage(Long messageId, Long userId) {
        log.info("User {} deletes message {}", userId, messageId);

        Message message = messageMapper.selectById(messageId);
        String status = normalizeStatus(message != null ? message.getStatus() : null);
        if (message == null || "deleted".equalsIgnoreCase(status) || message.getDeleted() == 1) {
            return Result.error(40702, "Message not found");
        }

        Long ownerId = resolveMessageUserId(message);
        if (ownerId == null || !ownerId.equals(userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }

        message.setStatus("deleted");
        message.setDeleted(1);
        message.setUpdatedAt(LocalDateTime.now());
        message.setUpdatedBy(userId);
        messageMapper.updateById(message);

        log.info("Message deleted");
        return Result.success();
    }

    @Override
    public Result<PageResult<MessageDTO>> listGoodsMessages(Long goodsId, Long page, Long size, Boolean purchasedOnly) {
        Page<Message> pageParam = new Page<>(page, size);
        Page<Message> result = messageMapper.selectByGoodsId(pageParam, goodsId, purchasedOnly);

        List<Message> messages = result.getRecords();
        Map<Long, List<MessageReply>> replyMap = new HashMap<>();
        Map<Long, List<Message>> legacyReplyMap = new HashMap<>();
        Set<Long> userIds = new HashSet<>();

        for (Message message : messages) {
            Long senderId = resolveMessageUserId(message);
            if (senderId != null) {
                userIds.add(senderId);
            }

            List<MessageReply> replies = messageReplyMapper.selectByMessageId(message.getId());
            if (replies != null && !replies.isEmpty()) {
                replyMap.put(message.getId(), replies);
                for (MessageReply reply : replies) {
                    if (reply.getReplierId() != null) {
                        userIds.add(reply.getReplierId());
                    }
                }
            } else {
                replyMap.put(message.getId(), List.of());
            }

            List<Message> legacyReplies = messageMapper.selectReplies(message.getId());
            if (legacyReplies != null && !legacyReplies.isEmpty()) {
                legacyReplyMap.put(message.getId(), legacyReplies);
                for (Message legacy : legacyReplies) {
                    Long legacySenderId = resolveMessageUserId(legacy);
                    if (legacySenderId != null) {
                        userIds.add(legacySenderId);
                    }
                }
            } else {
                legacyReplyMap.put(message.getId(), List.of());
            }
        }

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            if (users != null) {
                for (User user : users) {
                    userMap.put(user.getId(), user);
                }
            }
        }

        List<MessageDTO> dtos = messages.stream()
                .map(message -> {
                    Long senderId = resolveMessageUserId(message);
                    User user = senderId != null ? userMap.get(senderId) : null;
                    String name = user != null ? user.getNickName() : null;
                    String avatar = user != null ? user.getAvatarUrl() : null;
                    MessageDTO dto = MessageDTO.fromMessage(message, name, avatar);

                    List<MessageDTO> replyDtos = new ArrayList<>();
                    List<MessageReply> replies = replyMap.getOrDefault(message.getId(), List.of());
                    for (MessageReply reply : replies) {
                        replyDtos.add(toReplyDTO(reply, userMap));
                    }

                    List<Message> legacyReplies = legacyReplyMap.getOrDefault(message.getId(), List.of());
                    if (!legacyReplies.isEmpty()) {
                        replyDtos.addAll(legacyReplies.stream()
                                .map(legacy -> {
                                    Long legacySenderId = resolveMessageUserId(legacy);
                                    User legacyUser = legacySenderId != null ? userMap.get(legacySenderId) : null;
                                    String legacyName = legacyUser != null ? legacyUser.getNickName() : null;
                                    String legacyAvatar = legacyUser != null ? legacyUser.getAvatarUrl() : null;
                                    return MessageDTO.fromMessage(legacy, legacyName, legacyAvatar);
                                })
                                .collect(Collectors.toList()));
                    }

                    if (!replyDtos.isEmpty()) {
                        replyDtos.sort(Comparator.comparing(MessageDTO::getCreatedAt));
                        dto.setReplies(replyDtos);
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        PageResult<MessageDTO> pageResult = new PageResult<>();
        pageResult.setRecords(dtos);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setPages(result.getPages());

        return Result.success(pageResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<MessageDTO> replyMessage(Long messageId, String content, Long userId) {
        Message message = messageMapper.selectById(messageId);
        String status = normalizeStatus(message != null ? message.getStatus() : null);
        if (message == null || message.getDeleted() == 1 || !"visible".equalsIgnoreCase(status)) {
            return Result.error(40701, "Message not found or not visible");
        }

        MessageReply reply = new MessageReply();
        reply.setMessageId(messageId);
        reply.setReplierId(userId);
        reply.setContent(content);
        reply.setStatus("visible");
        LocalDateTime now = LocalDateTime.now();
        reply.setCreatedAt(now);
        reply.setUpdatedAt(now);
        reply.setUpdatedBy(userId);
        messageReplyMapper.insert(reply);

        return Result.success(toReplyDTO(reply));
    }

    @Override
    public Result<PageResult<MessageDTO>> listAdminMessages(String status, Long page, Long size) {
        Page<Message> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Message::getParentId)
                .orderByDesc(Message::getCreatedAt);
        if (status != null && !status.isBlank()) {
            wrapper.eq(Message::getStatus, status);
        }

        Page<Message> result = messageMapper.selectPage(pageParam, wrapper);
        List<MessageDTO> dtos = result.getRecords().stream()
                .map(message -> {
                    Long senderId = resolveMessageUserId(message);
                    User user = senderId != null ? userMapper.selectById(senderId) : null;
                    String name = user != null ? user.getNickName() : null;
                    String avatar = user != null ? user.getAvatarUrl() : null;
                    return MessageDTO.fromMessage(message, name, avatar);
                })
                .collect(Collectors.toList());

        PageResult<MessageDTO> pageResult = new PageResult<>();
        pageResult.setRecords(dtos);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setPages(result.getPages());
        return Result.success(pageResult);
    }

    @Override
    public Result<PageResult<MessageDTO>> listAdminReplies(String status, Long page, Long size) {
        Page<MessageReply> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MessageReply> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            wrapper.eq(MessageReply::getStatus, status);
        }
        wrapper.orderByDesc(MessageReply::getCreatedAt);

        Page<MessageReply> result = messageReplyMapper.selectPage(pageParam, wrapper);
        List<MessageDTO> dtos = result.getRecords().stream()
                .map(this::toReplyDTO)
                .collect(Collectors.toList());

        PageResult<MessageDTO> pageResult = new PageResult<>();
        pageResult.setRecords(dtos);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setPages(result.getPages());
        return Result.success(pageResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> hideMessage(Long messageId, Long adminId) {
        Message message = messageMapper.selectById(messageId);
        String status = normalizeStatus(message != null ? message.getStatus() : null);
        if (message == null || !"visible".equalsIgnoreCase(status)) {
            return Result.error(ErrorCode.CONFLICT);
        }
        message.setStatus("hidden");
        message.setUpdatedAt(LocalDateTime.now());
        message.setUpdatedBy(adminId);
        messageMapper.updateById(message);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteMessageByAdmin(Long messageId, Long adminId) {
        Message message = messageMapper.selectById(messageId);
        String status = normalizeStatus(message != null ? message.getStatus() : null);
        if (message == null || "deleted".equalsIgnoreCase(status)) {
            return Result.error(ErrorCode.CONFLICT);
        }
        message.setStatus("deleted");
        message.setDeleted(1);
        message.setUpdatedAt(LocalDateTime.now());
        message.setUpdatedBy(adminId);
        messageMapper.updateById(message);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> hideReply(Long replyId, Long adminId) {
        MessageReply reply = messageReplyMapper.selectById(replyId);
        String status = normalizeStatus(reply != null ? reply.getStatus() : null);
        if (reply == null || !"visible".equalsIgnoreCase(status)) {
            return Result.error(ErrorCode.CONFLICT);
        }
        reply.setStatus("hidden");
        reply.setUpdatedAt(LocalDateTime.now());
        reply.setUpdatedBy(adminId);
        messageReplyMapper.updateById(reply);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteReplyByAdmin(Long replyId, Long adminId) {
        MessageReply reply = messageReplyMapper.selectById(replyId);
        String status = normalizeStatus(reply != null ? reply.getStatus() : null);
        if (reply == null || "deleted".equalsIgnoreCase(status)) {
            return Result.error(ErrorCode.CONFLICT);
        }
        reply.setStatus("deleted");
        reply.setUpdatedAt(LocalDateTime.now());
        reply.setUpdatedBy(adminId);
        messageReplyMapper.updateById(reply);
        return Result.success();
    }

    private Long resolveMessageUserId(Message message) {
        if (message == null) {
            return null;
        }
        return message.getUserId() != null ? message.getUserId() : message.getSenderId();
    }

    private MessageDTO toReplyDTO(MessageReply reply) {
        Long userId = reply.getReplierId();
        User user = userId != null ? userMapper.selectById(userId) : null;
        String name = user != null ? user.getNickName() : null;
        String avatar = user != null ? user.getAvatarUrl() : null;

        MessageDTO dto = new MessageDTO();
        dto.setId(reply.getId());
        dto.setParentId(reply.getMessageId());
        dto.setSenderId(userId);
        dto.setSenderName(name != null ? name : "User" + userId);
        dto.setSenderAvatar(avatar);
        dto.setContent(reply.getContent());
        dto.setStatus(reply.getStatus());
        dto.setCreatedAt(reply.getCreatedAt());
        dto.setUpdatedAt(reply.getUpdatedAt());
        dto.setUpdatedBy(reply.getUpdatedBy());
        return dto;
    }

    private MessageDTO toReplyDTO(MessageReply reply, Map<Long, User> userMap) {
        Long userId = reply.getReplierId();
        User user = userId != null ? userMap.get(userId) : null;
        String name = user != null ? user.getNickName() : null;
        String avatar = user != null ? user.getAvatarUrl() : null;

        MessageDTO dto = new MessageDTO();
        dto.setId(reply.getId());
        dto.setParentId(reply.getMessageId());
        dto.setSenderId(userId);
        dto.setSenderName(name != null ? name : "User" + userId);
        dto.setSenderAvatar(avatar);
        dto.setContent(reply.getContent());
        dto.setStatus(reply.getStatus());
        dto.setCreatedAt(reply.getCreatedAt());
        dto.setUpdatedAt(reply.getUpdatedAt());
        dto.setUpdatedBy(reply.getUpdatedBy());
        return dto;
    }

    private String normalizeStatus(String status) {
        return status == null ? "visible" : status;
    }
}
