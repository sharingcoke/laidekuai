package com.laidekuai.address.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laidekuai.address.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 收货地址Mapper接口
 *
 * @author Laidekuai Team
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {

    /**
     * 根据用户ID查询地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Address> selectByUserId(@Param("userId") Long userId);

    /**
     * 统计用户地址数量
     *
     * @param userId 用户ID
     * @return 地址数量
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 清除用户所有默认地址标记
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE address SET is_default = 0, updated_at = NOW() WHERE user_id = #{userId} AND is_default = 1 AND deleted = 0")
    int clearDefaultByUserId(@Param("userId") Long userId);

    /**
     * 获取用户默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    Address selectDefaultByUserId(@Param("userId") Long userId);
}
