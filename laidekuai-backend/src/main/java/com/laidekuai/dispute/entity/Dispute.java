package com.laidekuai.dispute.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 争议记录实体
 */
@Data
@TableName("dispute")
public class Dispute {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long applicantId;

    private String applicantType;

    private String reason;

    private String status;

    private String resolution;

    private Long adminId;

    private String adminNote;

    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;
}
