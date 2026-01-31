package com.laidekuai.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laidekuai.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单Mapper接口
 *
 * @author Laidekuai Team
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 统计买家活跃订单数量（PENDING_PAY + PAID）
     *
     * @param buyerId 买家ID
     * @return 活跃订单数
     */
    int countActiveOrders(@Param("buyerId") Long buyerId);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单
     */
    Order selectByOrderNo(@Param("orderNo") String orderNo);
}
