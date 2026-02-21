package com.laidekuai.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 留言回复实体
 */
@Data
@TableName("message_reply")
public class MessageReply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long messageId;

    private Long replierId;

    private String content;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long updatedBy;
}
