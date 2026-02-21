package com.laidekuai.dispute.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.dispute.dto.DisputeDTO;
import com.laidekuai.dispute.dto.DisputeDetailDTO;
import com.laidekuai.dispute.entity.Dispute;
import com.laidekuai.dispute.mapper.DisputeMapper;
import com.laidekuai.dispute.service.DisputeService;
import com.laidekuai.goods.mapper.GoodsMapper;
import com.laidekuai.order.entity.Order;
import com.laidekuai.order.entity.OrderItem;
import com.laidekuai.order.mapper.OrderItemMapper;
import com.laidekuai.order.mapper.OrderMapper;
import com.laidekuai.user.entity.User;
import com.laidekuai.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final DisputeMapper disputeMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;

    @Override
    public Result<PageResult<DisputeDTO>> listAdminDisputes(String status, Long orderId, Long page, Long size) {
        Page<Dispute> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Dispute> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            wrapper.eq(Dispute::getStatus, status);
        }
        if (orderId != null) {
            wrapper.eq(Dispute::getOrderId, orderId);
        }
        wrapper.orderByDesc(Dispute::getCreatedAt);

        Page<Dispute> result = disputeMapper.selectPage(pageParam, wrapper);
        List<Dispute> disputes = result.getRecords();
        if (disputes.isEmpty()) {
            PageResult<DisputeDTO> pageResult = new PageResult<>();
            pageResult.setRecords(Collections.emptyList());
            pageResult.setTotal(0L);
            pageResult.setCurrent(result.getCurrent());
            pageResult.setSize(result.getSize());
            return Result.success(pageResult);
        }

        Set<Long> orderIds = disputes.stream().map(Dispute::getOrderId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Order> orderMap = orderIds.isEmpty() ? Collections.emptyMap() :
                orderMapper.selectBatchIds(orderIds).stream().collect(Collectors.toMap(Order::getId, Function.identity()));

        Set<Long> userIds = disputes.stream().map(Dispute::getApplicantId).filter(Objects::nonNull).collect(Collectors.toSet());
        orderMap.values().forEach(order -> {
            userIds.add(order.getBuyerId());
            userIds.add(order.getSellerId());
        });
        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap() :
                userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<DisputeDTO> dtos = disputes.stream().map(dispute -> {
            DisputeDTO dto = new DisputeDTO();
            dto.setId(dispute.getId());
            dto.setOrderId(dispute.getOrderId());
            dto.setApplicantId(dispute.getApplicantId());
            dto.setApplicantType(dispute.getApplicantType());
            dto.setReason(dispute.getReason());
            dto.setStatus(dispute.getStatus());
            dto.setCreatedAt(dispute.getCreatedAt());

            Order order = orderMap.get(dispute.getOrderId());
            if (order != null) {
                dto.setOrderNo(order.getOrderNo());
                dto.setBuyerId(order.getBuyerId());
                dto.setSellerId(order.getSellerId());
                dto.setAmount(order.getTotalAmount());
                dto.setRefundRequestCount(order.getRefundRequestCount());
            }

            dto.setApplicantName(resolveUserName(userMap.get(dispute.getApplicantId())));
            dto.setBuyerName(resolveUserName(userMap.get(dto.getBuyerId())));
            dto.setSellerName(resolveUserName(userMap.get(dto.getSellerId())));

            return dto;
        }).collect(Collectors.toList());

        PageResult<DisputeDTO> pageResult = new PageResult<>();
        pageResult.setRecords(dtos);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());

        return Result.success(pageResult);
    }

    @Override
    public Result<DisputeDetailDTO> getDisputeDetail(Long disputeId, Long userId, boolean isAdmin) {
        Dispute dispute = disputeMapper.selectById(disputeId);
        if (dispute == null) {
            return Result.error(ErrorCode.DISPUTE_NOT_FOUND);
        }
        if (!isAdmin && !Objects.equals(dispute.getApplicantId(), userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }

        Order order = orderMapper.selectById(dispute.getOrderId());
        if (order == null) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }

        Set<Long> userIds = Set.of(order.getBuyerId(), order.getSellerId(), dispute.getApplicantId());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        DisputeDetailDTO dto = new DisputeDetailDTO();
        dto.setId(dispute.getId());
        dto.setOrderId(dispute.getOrderId());
        dto.setOrderNo(order.getOrderNo());
        dto.setBuyerId(order.getBuyerId());
        dto.setBuyerName(resolveUserName(userMap.get(order.getBuyerId())));
        dto.setSellerId(order.getSellerId());
        dto.setSellerName(resolveUserName(userMap.get(order.getSellerId())));
        dto.setApplicantId(dispute.getApplicantId());
        dto.setApplicantName(resolveUserName(userMap.get(dispute.getApplicantId())));
        dto.setApplicantType(dispute.getApplicantType());
        dto.setAmount(order.getTotalAmount());
        dto.setReason(dispute.getReason());
        dto.setStatus(dispute.getStatus());
        dto.setRefundRequestCount(order.getRefundRequestCount());
        dto.setResolution(dispute.getResolution());
        dto.setAdminNote(dispute.getAdminNote());
        dto.setCreatedAt(dispute.getCreatedAt());
        dto.setResolvedAt(dispute.getResolvedAt());

        return Result.success(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> resolveDispute(Long disputeId, String resolution, String adminNote, Long adminId) {
        Dispute dispute = disputeMapper.selectById(disputeId);
        if (dispute == null) {
            return Result.error(ErrorCode.DISPUTE_NOT_FOUND);
        }
        if ("RESOLVED".equals(dispute.getStatus())) {
            return Result.error(ErrorCode.DISPUTE_ALREADY_RESOLVED);
        }

        Order order = orderMapper.selectById(dispute.getOrderId());
        if (order == null) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!"DISPUTED".equals(order.getStatus())) {
            return Result.error(ErrorCode.ORDER_STATUS_ERROR);
        }

        LocalDateTime now = LocalDateTime.now();
        if ("REFUND".equalsIgnoreCase(resolution)) {
            List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
            for (OrderItem item : items) {
                goodsMapper.releaseStock(item.getGoodsId(), item.getQuantity());
                log.debug("释放商品 {} 库存: {}", item.getGoodsId(), item.getQuantity());
            }
            order.setStatus("REFUNDED");
            order.setCancelReason("ADMIN");
            order.setCancelTime(now);
            order.setRefundRequestCount(0);
            order.setUpdatedAt(now);
            orderMapper.updateById(order);
            orderItemMapper.updateStatusByOrderId(order.getId(), "REFUNDED");
            orderItemMapper.updateOrderStatusByOrderId(order.getId(), "REFUNDED");
        } else if ("COMPLETE".equalsIgnoreCase(resolution)) {
            order.setStatus("COMPLETED");
            order.setIsSettled(1);
            order.setSettledTime(now);
            order.setRefundRequestCount(0);
            order.setUpdatedAt(now);
            orderMapper.updateById(order);
            orderItemMapper.updateStatusByOrderId(order.getId(), "COMPLETED");
            orderItemMapper.updateOrderStatusByOrderId(order.getId(), "COMPLETED");
        } else {
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        dispute.setStatus("RESOLVED");
        dispute.setResolution(resolution.toUpperCase());
        dispute.setAdminId(adminId);
        dispute.setAdminNote(adminNote);
        dispute.setResolvedAt(now);
        disputeMapper.updateById(dispute);

        return Result.success();
    }

    private String resolveUserName(User user) {
        if (user == null) {
            return null;
        }
        if (user.getNickName() != null && !user.getNickName().isBlank()) {
            return user.getNickName();
        }
        return user.getUsername();
    }
}
