package com.laidekuai.address.service.impl;

import com.laidekuai.address.dto.AddressRequest;
import com.laidekuai.address.entity.Address;
import com.laidekuai.address.mapper.AddressMapper;
import com.laidekuai.address.service.AddressService;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收货地址服务实现类
 *
 * @author Laidekuai Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    /**
     * 每个用户最大地址数量
     */
    private static final int MAX_ADDRESS_COUNT = 20;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Address> createAddress(AddressRequest request, Long userId) {
        log.info("用户 {} 创建收货地址", userId);

        // 检查地址数量限制
        int count = addressMapper.countByUserId(userId);
        if (count >= MAX_ADDRESS_COUNT) {
            log.warn("用户 {} 地址数量已达上限: {}", userId, count);
            return Result.error(ErrorCode.ADDRESS_LIMIT_EXCEEDED);
        }

        // 创建地址
        Address address = new Address();
        address.setUserId(userId);
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());
        address.setStatus("ACTIVE");
        address.setCreatedAt(LocalDateTime.now());

        // 处理默认地址
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            // 先清除其他默认地址
            addressMapper.clearDefaultByUserId(userId);
            address.setIsDefault(1);
        } else {
            // 如果是第一个地址，自动设为默认
            if (count == 0) {
                address.setIsDefault(1);
            } else {
                address.setIsDefault(0);
            }
        }

        addressMapper.insert(address);
        log.info("地址创建成功，ID: {}", address.getId());

        return Result.success(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Address> updateAddress(Long addressId, AddressRequest request, Long userId) {
        log.info("用户 {} 更新地址 {}", userId, addressId);

        // 查询地址并校验归属
        Address address = addressMapper.selectById(addressId);
        if (address == null || address.getDeleted() == 1) {
            return Result.error(ErrorCode.ADDRESS_NOT_FOUND);
        }
        if (!address.getUserId().equals(userId)) {
            log.warn("用户 {} 无权修改地址 {}", userId, addressId);
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // 更新字段
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());
        address.setUpdatedAt(LocalDateTime.now());

        // 处理默认地址
        if (Boolean.TRUE.equals(request.getIsDefault()) && address.getIsDefault() != 1) {
            addressMapper.clearDefaultByUserId(userId);
            address.setIsDefault(1);
        }

        addressMapper.updateById(address);
        log.info("地址更新成功，ID: {}", addressId);

        return Result.success(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteAddress(Long addressId, Long userId) {
        log.info("用户 {} 删除地址 {}", userId, addressId);

        // 查询地址并校验归属
        Address address = addressMapper.selectById(addressId);
        if (address == null || address.getDeleted() == 1) {
            return Result.error(ErrorCode.ADDRESS_NOT_FOUND);
        }
        if (!address.getUserId().equals(userId)) {
            log.warn("用户 {} 无权删除地址 {}", userId, addressId);
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // 软删除
        addressMapper.deleteById(addressId);
        log.info("地址删除成功，ID: {}", addressId);

        // 如果删除的是默认地址，将最新的地址设为默认
        if (address.getIsDefault() == 1) {
            List<Address> remaining = addressMapper.selectByUserId(userId);
            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setIsDefault(1);
                newDefault.setUpdatedAt(LocalDateTime.now());
                addressMapper.updateById(newDefault);
                log.info("自动设置新默认地址，ID: {}", newDefault.getId());
            }
        }

        return Result.success();
    }

    @Override
    public Result<List<Address>> listAddresses(Long userId) {
        log.debug("查询用户 {} 的地址列表", userId);
        List<Address> addresses = addressMapper.selectByUserId(userId);
        return Result.success(addresses);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> setDefaultAddress(Long addressId, Long userId) {
        log.info("用户 {} 设置默认地址 {}", userId, addressId);

        // 查询地址并校验归属
        Address address = addressMapper.selectById(addressId);
        if (address == null || address.getDeleted() == 1) {
            return Result.error(ErrorCode.ADDRESS_NOT_FOUND);
        }
        if (!address.getUserId().equals(userId)) {
            log.warn("用户 {} 无权修改地址 {}", userId, addressId);
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // 已是默认地址
        if (address.getIsDefault() == 1) {
            return Result.success();
        }

        // 清除其他默认地址
        addressMapper.clearDefaultByUserId(userId);

        // 设置当前地址为默认
        address.setIsDefault(1);
        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(address);

        log.info("默认地址设置成功，ID: {}", addressId);
        return Result.success();
    }

    @Override
    public Result<Address> getAddress(Long addressId, Long userId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || address.getDeleted() == 1) {
            return Result.error(ErrorCode.ADDRESS_NOT_FOUND);
        }
        if (!address.getUserId().equals(userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }
        return Result.success(address);
    }

    @Override
    public Result<Address> getDefaultAddress(Long userId) {
        Address address = addressMapper.selectDefaultByUserId(userId);
        if (address == null) {
            return Result.error(ErrorCode.ADDRESS_NOT_FOUND);
        }
        return Result.success(address);
    }
}
