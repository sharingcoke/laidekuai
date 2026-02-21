package com.laidekuai.common.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.laidekuai.common.dto.Result;
import com.laidekuai.order.entity.Order;
import com.laidekuai.order.mapper.OrderMapper;
import com.laidekuai.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    private static final int SCAN_LIMIT = 200;

    @Value("${app.order.timeout-minutes:15}")
    private Integer timeoutMinutes;

    /**
     * 每分钟检查一次超时未支付订单
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void cancelTimeoutOrders() {
        log.debug("开始检查超时订单...");

        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PENDING_PAY")
               .eq("deleted", 0)
               .apply("created_at <= DATE_SUB(NOW(), INTERVAL {0} MINUTE)", timeoutMinutes)
               .orderByAsc("created_at")
               .last("LIMIT " + SCAN_LIMIT);

        List<Order> timeoutOrders = orderMapper.selectList(wrapper);

        int canceledCount = 0;
        for (Order order : timeoutOrders) {
            try {
                if (!"PENDING_PAY".equals(order.getStatus())) {
                    continue;
                }
                Result<Void> result = orderService.cancelOrderSystem(order.getId());
                if (result != null && result.isSuccess()) {
                    Order refreshed = orderMapper.selectById(order.getId());
                    if (refreshed != null
                            && "CANCELED".equals(refreshed.getStatus())
                            && "TIMEOUT_CANCELED".equals(refreshed.getCancelReason())) {
                        canceledCount++;
                    }
                }
            } catch (Exception e) {
                log.error("自动取消订单 {} 失败", order.getOrderNo(), e);
            }
        }

        log.info("超时订单扫描 {} 条，成功取消 {} 条", timeoutOrders.size(), canceledCount);
    }
}
