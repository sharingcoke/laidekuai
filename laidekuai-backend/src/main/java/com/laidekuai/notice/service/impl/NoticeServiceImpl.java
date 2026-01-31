package com.laidekuai.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.notice.dto.NoticeDTO;
import com.laidekuai.notice.entity.Notice;
import com.laidekuai.notice.mapper.NoticeMapper;
import com.laidekuai.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务实现类
 *
 * @author Laidekuai Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Override
    public Result<PageResult<NoticeDTO>> listPublishedNotices(Long page, Long size) {
        Page<Notice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getStatus, "PUBLISHED")
                .eq(Notice::getDeleted, 0)
                .orderByDesc(Notice::getPublishedAt);

        Page<Notice> result = noticeMapper.selectPage(pageParam, wrapper);
        return Result.success(convertPage(result));
    }

    @Override
    public Result<PageResult<NoticeDTO>> listAllNotices(Long page, Long size) {
        Page<Notice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getDeleted, 0)
                .orderByDesc(Notice::getCreatedAt);

        Page<Notice> result = noticeMapper.selectPage(pageParam, wrapper);
        return Result.success(convertPage(result));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createNotice(String title, String content) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setStatus("DRAFT"); // 默认草稿
        notice.setCreatedAt(LocalDateTime.now());
        noticeMapper.insert(notice);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateNotice(Long id, String title, String content) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getDeleted() == 1) {
            return Result.error(404, "公告不存在");
        }
        if (title != null) notice.setTitle(title);
        if (content != null) notice.setContent(content);
        notice.setUpdatedAt(LocalDateTime.now());
        noticeMapper.updateById(notice);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteNotice(Long id) {
        noticeMapper.deleteById(id);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> publishNotice(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getDeleted() == 1) {
            return Result.error(404, "公告不存在");
        }
        notice.setStatus("PUBLISHED");
        notice.setPublishedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        noticeMapper.updateById(notice);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> offlineNotice(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getDeleted() == 1) {
            return Result.error(404, "公告不存在");
        }
        notice.setStatus("OFFLINE");
        notice.setUpdatedAt(LocalDateTime.now());
        noticeMapper.updateById(notice);
        return Result.success();
    }

    private PageResult<NoticeDTO> convertPage(Page<Notice> page) {
        List<NoticeDTO> dtos = page.getRecords().stream().map(notice -> {
            NoticeDTO dto = new NoticeDTO();
            BeanUtils.copyProperties(notice, dto);
            return dto;
        }).collect(Collectors.toList());

        PageResult<NoticeDTO> pageResult = new PageResult<>();
        pageResult.setRecords(dtos);
        pageResult.setTotal(page.getTotal());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setSize(page.getSize());
        return pageResult;
    }
}
