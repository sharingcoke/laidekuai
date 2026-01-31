package com.laidekuai.address.controller;

import com.laidekuai.address.dto.AddressRequest;
import com.laidekuai.address.entity.Address;
import com.laidekuai.address.service.AddressService;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址控制器
 *
 * @author Laidekuai Team
 */
@Slf4j
@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 新增收货地址
     * POST /api/addresses
     */
    @PostMapping
    public Result<Address> createAddress(@Valid @RequestBody AddressRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return addressService.createAddress(request, userId);
    }

    /**
     * 更新收货地址
     * PUT /api/addresses/{id}
     */
    @PutMapping("/{id}")
    public Result<Address> updateAddress(
            @PathVariable("id") Long addressId,
            @Valid @RequestBody AddressRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return addressService.updateAddress(addressId, request, userId);
    }

    /**
     * 删除收货地址
     * DELETE /api/addresses/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(@PathVariable("id") Long addressId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return addressService.deleteAddress(addressId, userId);
    }

    /**
     * 获取我的地址列表
     * GET /api/addresses
     */
    @GetMapping
    public Result<List<Address>> listAddresses() {
        Long userId = SecurityUtils.getCurrentUserId();
        return addressService.listAddresses(userId);
    }

    /**
     * 获取地址详情
     * GET /api/addresses/{id}
     */
    @GetMapping("/{id}")
    public Result<Address> getAddress(@PathVariable("id") Long addressId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return addressService.getAddress(addressId, userId);
    }

    /**
     * 设置默认地址
     * PUT /api/addresses/{id}/default
     */
    @PutMapping("/{id}/default")
    public Result<Void> setDefaultAddress(@PathVariable("id") Long addressId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return addressService.setDefaultAddress(addressId, userId);
    }

    /**
     * 获取默认地址
     * GET /api/addresses/default
     */
    @GetMapping("/default")
    public Result<Address> getDefaultAddress() {
        Long userId = SecurityUtils.getCurrentUserId();
        return addressService.getDefaultAddress(userId);
    }
}
