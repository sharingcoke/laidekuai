package com.laidekuai.dispute.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 争议详情 DTO
 */
@Data
public class DisputeDetailDTO {

    private Long id;
    private Long orderId;
    private String orderNo;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private Long applicantId;
    private String applicantName;
    private String applicantType;
    private BigDecimal amount;
    private String reason;
    private String status;
    private Integer refundRequestCount;
    private String resolution;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
