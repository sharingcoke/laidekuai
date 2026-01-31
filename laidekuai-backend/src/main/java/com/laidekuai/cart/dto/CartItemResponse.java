package com.laidekuai.cart.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车项响应
 *
 * @author Laidekuai Team
 */
@Data
public class CartItemResponse {

    /**
     * 购物车ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品标题
     */
    private String goodsTitle;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 商品状态（用于判断商品是否可售）
     */
    private String goodsStatus;

    /**
     * 商品库存（用于判断库存是否充足）
     */
    private Integer goodsStock;

    /**
     * 小计（价格 * 数量）
     */
    public BigDecimal getSubtotal() {
        if (goodsPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return goodsPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
