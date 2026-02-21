package com.laidekuai.notice.controller;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.notice.dto.NoticeDTO;
import com.laidekuai.notice.service.NoticeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 公告控制器
 *
 * @author Laidekuai Team
 */
@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 公开接口

    /**
     * 获取已发布公告
     */
    @GetMapping("/notices")
    public Result<PageResult<NoticeDTO>> listPublishedNotices(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        return noticeService.listPublishedNotices(page, size);
    }

    /**
     * 公告详情（已发布）
     */
    @GetMapping("/notices/{id}")
    public Result<NoticeDTO> getNoticeDetail(@PathVariable Long id) {
        return noticeService.getPublishedNotice(id);
    }

    // 管理员接口

    /**
     * 管理员获取所有公告
     */
    @GetMapping("/admin/notices")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<NoticeDTO>> listAllNotices(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        return noticeService.listAllNotices(page, size);
    }

    /**
     * 创建公告
     */
    @PostMapping("/admin/notices")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createNotice(@RequestBody NoticeForm form) {
        return noticeService.createNotice(form.getTitle(), form.getContent());
    }

    /**
     * 更新公告
     */
    @PutMapping("/admin/notices/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateNotice(@PathVariable Long id, @RequestBody NoticeForm form) {
        return noticeService.updateNotice(id, form.getTitle(), form.getContent());
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/admin/notices/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        return noticeService.deleteNotice(id);
    }

    /**
     * 发布公告
     */
    @PostMapping("/admin/notices/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> publishNotice(@PathVariable Long id) {
        return noticeService.publishNotice(id);
    }

    /**
     * 下线公告
     */
    @PostMapping("/admin/notices/{id}/offline")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> offlineNotice(@PathVariable Long id) {
        return noticeService.offlineNotice(id);
    }

    @Data
    public static class NoticeForm {
        private String title;
        private String content;
    }
}
