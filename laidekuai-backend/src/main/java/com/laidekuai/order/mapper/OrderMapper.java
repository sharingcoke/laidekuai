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

    /**
     * 条件取消订单（仅 PENDING_PAY）
     *
     * @param orderId      订单ID
     * @param cancelReason 取消原因
     * @param cancelTime   取消时间
     * @param updatedAt    更新时间
     * @return 影响行数
     */
    int markCanceledIfPending(@Param("orderId") Long orderId,
                              @Param("cancelReason") String cancelReason,
                              @Param("cancelTime") java.time.LocalDateTime cancelTime,
                              @Param("updatedAt") java.time.LocalDateTime updatedAt);

    /**
     * 条件更新订单为已发货（仅 PAID）
     *
     * @param orderId   订单ID
     * @param updatedAt 更新时间
     * @return 影响行数
     */
    int markShippedIfPaid(@Param("orderId") Long orderId,
                          @Param("updatedAt") java.time.LocalDateTime updatedAt);
}
