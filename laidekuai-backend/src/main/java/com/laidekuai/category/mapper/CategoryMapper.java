package com.laidekuai.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laidekuai.category.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类Mapper
 *
 * @author Laidekuai Team
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
