package com.laidekuai.order.dto;

import com.laidekuai.order.entity.Order;
import com.laidekuai.order.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单响应DTO
 *
 * @author Laidekuai Team
 */
@Data
public class OrderDTO {

    private Long id;
    private String orderNo;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private String status;
    private String refundReason;
    private String remark;
    private Integer refundRequestCount;
    private LocalDateTime payTime;
    private LocalDateTime disputeTime;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private LocalDateTime createdAt;
    
    private List<OrderItemDTO> items;

    /**
     * 从Order实体转换
     */
    public static OrderDTO fromOrder(Order order, List<OrderItem> orderItems) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setBuyerId(order.getBuyerId());
        dto.setSellerId(order.getSellerId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setStatus(order.getStatus());
        dto.setRefundReason(order.getRefundReason());
        dto.setRemark(order.getRemark());
        dto.setRefundRequestCount(order.getRefundRequestCount());
        dto.setPayTime(order.getPayTime());
        dto.setDisputeTime(order.getDisputeTime());
        dto.setReceiverName(order.getReceiverName());
        dto.setReceiverPhone(order.getReceiverPhone());
        dto.setReceiverAddress(order.getReceiverAddress());
        dto.setCreatedAt(order.getCreatedAt());
        
        if (orderItems != null) {
            dto.setItems(orderItems.stream()
                    .map(OrderItemDTO::fromOrderItem)
                    .toList());
        }
        return dto;
    }
}
