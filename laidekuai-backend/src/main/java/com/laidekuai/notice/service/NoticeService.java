package com.laidekuai.notice.service;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.notice.dto.NoticeDTO;
import com.laidekuai.notice.entity.Notice;

/**
 * 公告服务接口
 *
 * @author Laidekuai Team
 */
public interface NoticeService {

    /**
     * 获取已发布公告列表（公开）
     *
     * @param page 页码
     * @param size 每页大小
     * @return 公告列表
     */
    Result<PageResult<NoticeDTO>> listPublishedNotices(Long page, Long size);

    /**
     * 获取已发布公告详情（公开）
     *
     * @param id 公告ID
     * @return 公告详情
     */
    Result<NoticeDTO> getPublishedNotice(Long id);

    /**
     * 获取所有公告列表（管理员）
     *
     * @param page 页码
     * @param size 每页大小
     * @return 公告列表
     */
    Result<PageResult<NoticeDTO>> listAllNotices(Long page, Long size);

    /**
     * 创建公告
     *
     * @param title   标题
     * @param content 内容
     * @return 创建结果
     */
    Result<Void> createNotice(String title, String content);

    /**
     * 更新公告
     *
     * @param id      公告ID
     * @param title   标题
     * @param content 内容
     * @return 更新结果
     */
    Result<Void> updateNotice(Long id, String title, String content);

    /**
     * 删除公告
     *
     * @param id 公告ID
     * @return 删除结果
     */
    Result<Void> deleteNotice(Long id);

    /**
     * 发布公告
     *
     * @param id 公告ID
     * @return 发布结果
     */
    Result<Void> publishNotice(Long id);

    /**
     * 下线公告
     *
     * @param id 公告ID
     * @return 下线结果
     */
    Result<Void> offlineNotice(Long id);
}
