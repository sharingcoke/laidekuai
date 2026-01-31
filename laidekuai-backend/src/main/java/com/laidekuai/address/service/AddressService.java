package com.laidekuai.address.service;

import com.laidekuai.address.dto.AddressRequest;
import com.laidekuai.address.entity.Address;
import com.laidekuai.common.dto.Result;

import java.util.List;

/**
 * 收货地址服务接口
 *
 * @author Laidekuai Team
 */
public interface AddressService {

    /**
     * 新增收货地址
     *
     * @param request 地址请求
     * @param userId  当前用户ID
     * @return 创建的地址
     */
    Result<Address> createAddress(AddressRequest request, Long userId);

    /**
     * 更新收货地址
     *
     * @param addressId 地址ID
     * @param request   地址请求
     * @param userId    当前用户ID
     * @return 更新后的地址
     */
    Result<Address> updateAddress(Long addressId, AddressRequest request, Long userId);

    /**
     * 删除收货地址
     *
     * @param addressId 地址ID
     * @param userId    当前用户ID
     * @return 删除结果
     */
    Result<Void> deleteAddress(Long addressId, Long userId);

    /**
     * 获取用户地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    Result<List<Address>> listAddresses(Long userId);

    /**
     * 设置默认地址
     *
     * @param addressId 地址ID
     * @param userId    当前用户ID
     * @return 设置结果
     */
    Result<Void> setDefaultAddress(Long addressId, Long userId);

    /**
     * 获取地址详情
     *
     * @param addressId 地址ID
     * @param userId    当前用户ID
     * @return 地址详情
     */
    Result<Address> getAddress(Long addressId, Long userId);

    /**
     * 获取用户默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    Result<Address> getDefaultAddress(Long userId);
}
