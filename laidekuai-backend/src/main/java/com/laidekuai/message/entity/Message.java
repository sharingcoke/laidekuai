package com.laidekuai.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品留言实体类
 *
 * @author Laidekuai Team
 */
@Data
@TableName("message")
public class Message {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long goodsId;

    @TableField("user_id")
    private Long userId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 父消息ID（用于回复）
     */
    private Long parentId;

    /**
     * 留言内容
     */
    private String content;

    private String status;

    @TableField("is_purchased")
    private Integer isPurchased;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long updatedBy;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
}
