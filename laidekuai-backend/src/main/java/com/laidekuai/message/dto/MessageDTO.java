package com.laidekuai.message.dto;

import com.laidekuai.message.entity.Message;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 留言响应DTO
 *
 * @author Laidekuai Team
 */
@Data
public class MessageDTO {

    private Long id;
    private Long goodsId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long parentId;
    private String content;
    private String status;
    private Integer isPurchased;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long updatedBy;
    private List<MessageDTO> replies;

    /**
     * 从Message实体转换
     */
    public static MessageDTO fromMessage(Message message, String senderName, String senderAvatar) {
        MessageDTO dto = new MessageDTO();
        Long senderId = message.getUserId() != null ? message.getUserId() : message.getSenderId();
        dto.setId(message.getId());
        dto.setGoodsId(message.getGoodsId());
        dto.setSenderId(senderId);
        dto.setSenderName(senderName != null ? senderName : "User" + senderId);
        dto.setSenderAvatar(senderAvatar);
        dto.setParentId(message.getParentId());
        dto.setContent(message.getContent());
        dto.setStatus(message.getStatus());
        dto.setIsPurchased(message.getIsPurchased());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        dto.setUpdatedBy(message.getUpdatedBy());
        return dto;
    }
}
