package com.laidekuai.dispute.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laidekuai.dispute.entity.Dispute;
import org.apache.ibatis.annotations.Mapper;

/**
 * 争议记录 Mapper
 */
@Mapper
public interface DisputeMapper extends BaseMapper<Dispute> {
}
