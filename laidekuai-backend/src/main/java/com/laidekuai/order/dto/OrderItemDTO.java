package com.laidekuai.order.dto;

import com.laidekuai.order.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单项响应DTO
 *
 * @author Laidekuai Team
 */
@Data
public class OrderItemDTO {

    private Long id;
    private Long orderId;
    private Long goodsId;
    private Long sellerId;
    private String goodsTitle;
    private String goodsCover;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal amount;
    private String itemStatus;
    private String shipCompany;
    private String trackingNo;

    /**
     * 从OrderItem实体转换
     */
    public static OrderItemDTO fromOrderItem(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setOrderId(item.getOrderId());
        dto.setGoodsId(item.getGoodsId());
        dto.setSellerId(item.getSellerId());
        dto.setGoodsTitle(item.getGoodsTitle());
        dto.setGoodsCover(item.getGoodsCover());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setAmount(item.getAmount());
        dto.setItemStatus(item.getItemStatus());
        dto.setShipCompany(item.getShipCompany());
        dto.setTrackingNo(item.getTrackingNo());
        return dto;
    }
}
