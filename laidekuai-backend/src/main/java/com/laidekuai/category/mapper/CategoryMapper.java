package com.laidekuai.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laidekuai.category.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 分类Mapper
 *
 * @author Laidekuai Team
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 统计分类下的商品数量
     *
     * @param categoryId 分类ID
     * @return 商品数量
     */
    long countGoodsByCategory(@Param("categoryId") Long categoryId);
}
