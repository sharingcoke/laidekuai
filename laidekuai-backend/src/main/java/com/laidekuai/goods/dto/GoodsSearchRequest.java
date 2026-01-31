package com.laidekuai.goods.dto;

import lombok.Data;

/**
 * 商品查询请求
 *
 * @author Laidekuai Team
 */
@Data
public class GoodsSearchRequest {

    /**
     * 关键词（前缀匹配）
     */
    private String keyword;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 页码
     */
    private Long page = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 排序字段（created_at/price/stock）
     */
    private String sortBy = "created_at";

    /**
     * 排序方向（asc/desc）
     */
    private String sortOrder = "desc";
}
