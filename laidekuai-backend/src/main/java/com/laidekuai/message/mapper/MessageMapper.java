package com.laidekuai.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.message.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 留言Mapper接口
 *
 * @author Laidekuai Team
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 查询商品的留言列表（仅主留言）
     *
     * @param goodsId 商品ID
     * @return 留言列表
     */
    Page<Message> selectByGoodsId(Page<Message> page,
                                  @Param("goodsId") Long goodsId,
                                  @Param("purchasedOnly") Boolean purchasedOnly);

    /**
     * 查询留言的回复列表
     *
     * @param parentId 父留言ID
     * @return 回复列表
     */
    List<Message> selectReplies(@Param("parentId") Long parentId);
}
