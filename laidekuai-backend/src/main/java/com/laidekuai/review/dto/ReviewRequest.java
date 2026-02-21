package com.laidekuai.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建评价请求DTO
 *
 * @author Laidekuai Team
 */
@Data
public class ReviewRequest {

    /**
     * 订单项ID（如需单独评价单个商品）
     */
    @NotNull(message = "订单项ID不能为空")
    private Long orderItemId;

    /**
     * 评分（1-5）
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer rating;

    /**
     * 评价内容
     */
    @Size(max = 500, message = "评价内容不能超过500个字符")
    private String content;

    /**
     * 评价图片列表
     */
    private List<String> images;

    /**
     * 是否匿名
     */
    private Boolean isAnonymous = false;
}
