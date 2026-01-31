package com.laidekuai.message.service;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.message.dto.MessageDTO;
import com.laidekuai.message.dto.MessageRequest;

/**
 * 留言服务接口
 *
 * @author Laidekuai Team
 */
public interface MessageService {

    /**
     * 发送留言
     *
     * @param request 留言请求
     * @param userId  当前用户ID
     * @return 留言结果
     */
    Result<MessageDTO> sendMessage(MessageRequest request, Long userId);

    /**
     * 删除留言（仅作者可删）
     *
     * @param messageId 留言ID
     * @param userId    当前用户ID
     * @return 删除结果
     */
    Result<Void> deleteMessage(Long messageId, Long userId);

    /**
     * 获取商品留言列表
     *
     * @param goodsId 商品ID
     * @param page    页码
     * @param size    每页大小
     * @return 留言列表（含回复）
     */
    Result<PageResult<MessageDTO>> listGoodsMessages(Long goodsId, Long page, Long size);
}
