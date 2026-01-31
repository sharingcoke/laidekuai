package com.laidekuai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laidekuai.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 *
 * @author Laidekuai Team
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
