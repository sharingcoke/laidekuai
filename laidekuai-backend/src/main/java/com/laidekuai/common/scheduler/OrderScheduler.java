package com.laidekuai.common.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laidekuai.order.entity.Order;
import com.laidekuai.order.mapper.OrderMapper;
import com.laidekuai.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务
 *
 * @author Laidekuai Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderMapper orderMapper;
    private final OrderService orderService;

    @Value("${app.order.timeout-minutes:15}")
    private Integer timeoutMinutes;

    /**
     * 每分钟检查一次超时未支付订单
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void cancelTimeoutOrders() {
        log.debug("开始检查超时订单...");
        
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
        
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, "PENDING_PAY")
               .eq(Order::getDeleted, 0)
               .lt(Order::getCreatedAt, timeoutTime); // 创建时间早于超市时间点
               
        List<Order> timeoutOrders = orderMapper.selectList(wrapper);
        
        if (timeoutOrders.isEmpty()) {
            return;
        }
        
        log.info("发现 {} 个超时未支付订单，开始处理...", timeoutOrders.size());
        
        for (Order order : timeoutOrders) {
            try {
                orderService.cancelOrderSystem(order.getId());
            } catch (Exception e) {
                log.error("自动取消订单 {} 失败", order.getOrderNo(), e);
            }
        }
    }
}
