package com.laidekuai.goods.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建商品请求
 *
 * @author Laidekuai Team
 */
@Data
public class GoodsCreateRequest {

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /**
     * 商品标题
     */
    @NotBlank(message = "商品标题不能为空")
    @Size(min = 1, max = 100, message = "商品标题长度必须在1-100个字符之间")
    private String title;

    /**
     * 副标题
     */
    @Size(max = 200, message = "副标题长度不能超过200个字符")
    private String subTitle;

    /**
     * 价格
     */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    /**
     * 库存
     */
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 商品图片URL列表
     */
    private List<String> imageUrls;
}
