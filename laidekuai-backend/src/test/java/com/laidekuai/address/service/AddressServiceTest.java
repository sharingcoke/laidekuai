package com.laidekuai.address.service;

import com.laidekuai.address.dto.AddressRequest;
import com.laidekuai.address.entity.Address;
import com.laidekuai.address.mapper.AddressMapper;
import com.laidekuai.address.service.impl.AddressServiceImpl;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 地址服务单元测试
 *
 * @author Laidekuai Team
 */
@SpringBootTest
@ActiveProfiles("test")
class AddressServiceTest {

    @MockBean
    private AddressMapper addressMapper;

    private AddressServiceImpl addressService;

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        addressService = new AddressServiceImpl(addressMapper);
    }

    // ========== 创建地址测试 ==========

    @Test
    @DisplayName("正常场景：创建第一个地址自动设为默认")
    void createAddress_FirstAddress_SetAsDefault() {
        // Given
        AddressRequest request = buildAddressRequest();
        when(addressMapper.countByUserId(USER_ID)).thenReturn(0);
        when(addressMapper.insert(any(Address.class))).thenAnswer(invocation -> {
            Address address = invocation.getArgument(0);
            address.setId(1L);
            return 1;
        });

        // When
        Result<Address> result = addressService.createAddress(request, USER_ID);

        // Then
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getIsDefault()).isEqualTo(1);
        assertThat(result.getData().getUserId()).isEqualTo(USER_ID);

        verify(addressMapper, times(1)).insert(any(Address.class));
    }

    @Test
    @DisplayName("正常场景：创建第二个地址不自动设为默认")
    void createAddress_SecondAddress_NotDefault() {
        // Given
        AddressRequest request = buildAddressRequest();
        request.setIsDefault(false);
        when(addressMapper.countByUserId(USER_ID)).thenReturn(1);
        when(addressMapper.insert(any(Address.class))).thenAnswer(invocation -> {
            Address address = invocation.getArgument(0);
            address.setId(2L);
            return 1;
        });

        // When
        Result<Address> result = addressService.createAddress(request, USER_ID);

        // Then
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData().getIsDefault()).isEqualTo(0);
    }

    @Test
    @DisplayName("正常场景：创建地址并设为默认，清除其他默认")
    void createAddress_SetAsDefault_ClearOthers() {
        // Given
        AddressRequest request = buildAddressRequest();
        request.setIsDefault(true);
        when(addressMapper.countByUserId(USER_ID)).thenReturn(2);
        when(addressMapper.clearDefaultByUserId(USER_ID)).thenReturn(1);
        when(addressMapper.insert(any(Address.class))).thenReturn(1);

        // When
        Result<Address> result = addressService.createAddress(request, USER_ID);

        // Then
        assertThat(result.getCode()).isEqualTo(0);
        verify(addressMapper, times(1)).clearDefaultByUserId(USER_ID);
    }

    @Test
    @DisplayName("异常场景：地址数量超过上限返回40702")
    void createAddress_ExceedLimit_Returns40702() {
        // Given
        AddressRequest request = buildAddressRequest();
        when(addressMapper.countByUserId(USER_ID)).thenReturn(20);

        // When
        Result<Address> result = addressService.createAddress(request, USER_ID);

        // Then
        assertThat(result.getCode()).isEqualTo(ErrorCode.ADDRESS_LIMIT_EXCEEDED.getCode());
        verify(addressMapper, never()).insert(any(Address.class));
    }

    // ========== 更新地址测试 ==========

    @Test
    @DisplayName("正常场景：更新自己的地址成功")
    void updateAddress_Self_Success() {
        // Given
        Long addressId = 1L;
        AddressRequest request = buildAddressRequest();
        request.setReceiverName("新姓名");

        Address existing = buildAddress(addressId, USER_ID, false);
        when(addressMapper.selectById(addressId)).thenReturn(existing);
        when(addressMapper.updateById(any(Address.class))).thenReturn(1);

        // When
        Result<Address> result = addressService.updateAddress(addressId, request, USER_ID);

        // Then
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData().getReceiverName()).isEqualTo("新姓名");
    }

    @Test
    @DisplayName("异常场景：更新不存在的地址返回40701")
    void updateAddress_NotFound_Returns40701() {
        // Given
        when(addressMapper.selectById(999L)).thenReturn(null);

        // When
        Result<Address> result = addressService.updateAddress(999L, buildAddressRequest(), USER_ID);

        // Then
        assertThat(result.getCode()).isEqualTo(ErrorCode.ADDRESS_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("异常场景：更新他人的地址返回403")
    void updateAddress_OtherUser_Returns403() {
        // Given
        Long addressId = 1L;
        Address existing = buildAddress(addressId, OTHER_USER_ID, false);
        when(addressMapper.selectById(addressId)).thenReturn(existing);

        // When
        Result<Address> result = addressService.updateAddress(addressId, buildAddressRequest(), USER_ID);

        // Then
        assertThat(result.getCode()).isEqualTo(ErrorCode.FORBIDDEN.getCode());
        verify(addressMapper, never()).updateById(any(Address.class));
    }

    // ========== 删除地址测试 ==========

    @Test
    @DisplayName("正常场景：删除自己的地址成功")
    void deleteAddress_Self_Success() {
        // Given
        Long addressId = 1L;
        Address existing = buildAddress(addressId, USER_ID, false);
        when(addressMapper.selectById(addressId)).thenReturn(existing);
        when(addressMapper.deleteById(addressId)).thenReturn(1);
        when(addressMapper.selectByUserId(USER_ID)).thenReturn(new ArrayList<>());

        // When
        Result<Void> result = addressService.deleteAddress(addressId, USER_ID);

        // Then
        assertThat(result.getCode()).isEqualTo(0);
        verify(addressMapper, times(1)).deleteById(addressId);
    }

    @Test
    @DisplayName("正常场景：删除默认地址后自动设置新默认")
    void deleteAddress_DefaultAddress_SetNewDefault() {
        // Given
        Long addressId = 1L;
        Address defaultAddress = buildAddress(addressId, USER_ID, true);
        Address remaining = buildAddress(2L, USER_ID, false);

        when(addressMapper.selectById(addressId)).thenReturn(defaultAddress);
        when(addressMapper.deleteById(addressId)).thenReturn(1);
        when(addressMapper.selectByUserId(USER_ID)).thenReturn(List.of(remaining));
        when(addressMapper.updateById(any(Address.class))).thenReturn(1);

        // When
        Result<Void> result = addressService.deleteAddress(addressId, USER_ID);

        // Then
        assertThat(result.getCode()).isEqualTo(0);

        // 验证设置了新的默认地址
        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressMapper).updateById(captor.capture());
        assertThat(captor.getValue().getIsDefault()).isEqualTo(1);
    }

    @Test
    @DisplayName("异常场景：删除他人的地址返回403")
    void deleteAddress_OtherUser_Returns403() {
        // Given
        Long addressId = 1L;
        Address existing = buildAddress(addressId, OTHER_USER_ID, false);
        when(addressMapper.selectById(addressId)).thenReturn(existing);

        // When
        Result<Void> result = addressService.deleteAddress(addressId, USER_ID);

        // Then
        assertThat(result.getCode()).isEqualTo(ErrorCode.FORBIDDEN.getCode());
        verify(addressMapper, never()).deleteById(anyLong());
    }

    // ========== 设置默认地址测试 ==========

    @Test
    @DisplayName("正常场景：设置默认地址成功")
    void setDefaultAddress_Success() {
        // Given
        Long addressId = 1L;
        Address existing = buildAddress(addressId, USER_ID, false);
        when(addressMapper.selectById(addressId)).thenReturn(existing);
        when(addressMapper.clearDefaultByUserId(USER_ID)).thenReturn(1);
        when(addressMapper.updateById(any(Address.class))).thenReturn(1);

        // When
        Result<Void> result = addressService.setDefaultAddress(addressId, USER_ID);

        // Then
        assertThat(result.getCode()).isEqualTo(0);
        verify(addressMapper, times(1)).clearDefaultByUserId(USER_ID);
    }

    @Test
    @DisplayName("正常场景：已是默认地址则直接返回成功")
    void setDefaultAddress_AlreadyDefault_Success() {
        // Given
        Long addressId = 1L;
        Address existing = buildAddress(addressId, USER_ID, true);
        when(addressMapper.selectById(addressId)).thenReturn(existing);

        // When
        Result<Void> result = addressService.setDefaultAddress(addressId, USER_ID);

        // Then
        assertThat(result.getCode()).isEqualTo(0);
        verify(addressMapper, never()).clearDefaultByUserId(any());
    }

    // ========== 查询地址测试 ==========

    @Test
    @DisplayName("正常场景：获取地址列表成功")
    void listAddresses_Success() {
        // Given
        List<Address> addresses = List.of(
                buildAddress(1L, USER_ID, true),
                buildAddress(2L, USER_ID, false)
        );
        when(addressMapper.selectByUserId(USER_ID)).thenReturn(addresses);

        // When
        Result<List<Address>> result = addressService.listAddresses(USER_ID);

        // Then
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData()).hasSize(2);
    }

    @Test
    @DisplayName("正常场景：获取默认地址成功")
    void getDefaultAddress_Success() {
        // Given
        Address defaultAddress = buildAddress(1L, USER_ID, true);
        when(addressMapper.selectDefaultByUserId(USER_ID)).thenReturn(defaultAddress);

        // When
        Result<Address> result = addressService.getDefaultAddress(USER_ID);

        // Then
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("异常场景：无默认地址返回40701")
    void getDefaultAddress_NotFound_Returns40701() {
        // Given
        when(addressMapper.selectDefaultByUserId(USER_ID)).thenReturn(null);

        // When
        Result<Address> result = addressService.getDefaultAddress(USER_ID);

        // Then
        assertThat(result.getCode()).isEqualTo(ErrorCode.ADDRESS_NOT_FOUND.getCode());
    }

    // ========== 辅助方法 ==========

    private AddressRequest buildAddressRequest() {
        AddressRequest request = new AddressRequest();
        request.setReceiverName("张三");
        request.setReceiverPhone("13800138000");
        request.setProvince("广东省");
        request.setCity("深圳市");
        request.setDistrict("南山区");
        request.setDetail("科技园路1号");
        request.setIsDefault(false);
        return request;
    }

    private Address buildAddress(Long id, Long userId, boolean isDefault) {
        Address address = new Address();
        address.setId(id);
        address.setUserId(userId);
        address.setReceiverName("测试");
        address.setReceiverPhone("13800138000");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setDistrict("南山区");
        address.setDetail("测试详细地址");
        address.setIsDefault(isDefault ? 1 : 0);
        address.setStatus("ACTIVE");
        address.setDeleted(0);
        return address;
    }
}
