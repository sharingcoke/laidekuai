package com.laidekuai.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laidekuai.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单项Mapper接口
 *
 * @author Laidekuai Team
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 根据订单ID查询订单项
     *
     * @param orderId 订单ID
     * @return 订单项列表
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 批量插入订单项
     *
     * @param items 订单项列表
     * @return 影响行数
     */
    int batchInsert(@Param("items") List<OrderItem> items);

    /**
     * 根据订单ID更新订单状态
     *
     * @param orderId 订单ID
     * @param status  状态
     * @return 影响行数
     */
    int updateStatusByOrderId(@Param("orderId") Long orderId, @Param("status") String status);
}
