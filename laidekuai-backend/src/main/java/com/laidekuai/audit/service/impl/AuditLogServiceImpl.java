package com.laidekuai.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.audit.dto.AuditLogDTO;
import com.laidekuai.audit.entity.AuditLog;
import com.laidekuai.audit.mapper.AuditLogMapper;
import com.laidekuai.audit.service.AuditLogService;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.order.entity.Order;
import com.laidekuai.order.mapper.OrderMapper;
import com.laidekuai.user.entity.User;
import com.laidekuai.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogMapper auditLogMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    @Override
    public void record(Long orderId, String action, Long operatorId, String operatorRole, String reason) {
        if (operatorId == null || !StringUtils.hasText(action)) {
            return;
        }
        AuditLog logRecord = new AuditLog();
        logRecord.setOrderId(orderId);
        logRecord.setAction(action);
        logRecord.setOperatorId(operatorId);
        logRecord.setOperatorRole(operatorRole);
        logRecord.setReason(reason);
        logRecord.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(logRecord);
    }

    @Override
    public Result<PageResult<AuditLogDTO>> listAdminLogs(String orderNo, String action, String operator, Long page, Long size) {
        long pageNo = (page == null || page <= 0) ? 1 : page;
        long pageSize = (size == null || size <= 0) ? 10 : Math.min(size, 50);
        Page<AuditLog> pageParam = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(action)) {
            wrapper.eq(AuditLog::getAction, action);
        }

        if (StringUtils.hasText(orderNo)) {
            List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .like(Order::getOrderNo, orderNo)
                    .select(Order::getId, Order::getOrderNo));
            if (orders.isEmpty()) {
                return Result.success(PageResult.of(List.of(), 0L, pageNo, pageSize));
            }
            wrapper.in(AuditLog::getOrderId, orders.stream().map(Order::getId).toList());
        }

        if (StringUtils.hasText(operator)) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .and(w -> w.like(User::getUsername, operator).or().like(User::getNickName, operator))
                    .select(User::getId, User::getUsername, User::getNickName));
            if (users.isEmpty()) {
                return Result.success(PageResult.of(List.of(), 0L, pageNo, pageSize));
            }
            wrapper.in(AuditLog::getOperatorId, users.stream().map(User::getId).toList());
        }

        wrapper.orderByDesc(AuditLog::getCreatedAt);
        Page<AuditLog> result = auditLogMapper.selectPage(pageParam, wrapper);

        List<AuditLog> records = result.getRecords();
        if (records.isEmpty()) {
            return Result.success(PageResult.of(List.of(), result.getTotal(), result.getCurrent(), result.getSize()));
        }

        Set<Long> orderIds = records.stream()
                .map(AuditLog::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Order> orderMap = orderIds.isEmpty()
                ? Collections.emptyMap()
                : orderMapper.selectBatchIds(orderIds).stream()
                    .collect(Collectors.toMap(Order::getId, o -> o));

        Set<Long> operatorIds = records.stream()
                .map(AuditLog::getOperatorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = operatorIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(operatorIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u));

        List<AuditLogDTO> dtos = new ArrayList<>();
        for (AuditLog logRecord : records) {
            AuditLogDTO dto = new AuditLogDTO();
            dto.setId(logRecord.getId());
            dto.setOrderId(logRecord.getOrderId());
            Order order = logRecord.getOrderId() == null ? null : orderMap.get(logRecord.getOrderId());
            dto.setOrderNo(order != null ? order.getOrderNo() : null);
            dto.setAction(logRecord.getAction());
            User user = userMap.get(logRecord.getOperatorId());
            if (user != null) {
                dto.setOperator(StringUtils.hasText(user.getNickName()) ? user.getNickName() : user.getUsername());
            }
            dto.setOperatorRole(logRecord.getOperatorRole());
            dto.setRemark(logRecord.getReason());
            dto.setCreatedAt(logRecord.getCreatedAt());
            dtos.add(dto);
        }

        PageResult<AuditLogDTO> pageResult = new PageResult<>(dtos, result.getTotal(), result.getCurrent(), result.getSize());
        return Result.success(pageResult);
    }
}
