package com.laidekuai.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laidekuai.notice.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告Mapper接口
 *
 * @author Laidekuai Team
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}
