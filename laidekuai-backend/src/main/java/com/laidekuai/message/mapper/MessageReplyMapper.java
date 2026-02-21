package com.laidekuai.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laidekuai.message.entity.MessageReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageReplyMapper extends BaseMapper<MessageReply> {

    List<MessageReply> selectByMessageId(@Param("messageId") Long messageId);
}
