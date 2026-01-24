package com.achievement.service.impl;

import com.achievement.domain.po.InterimResultView;
import com.achievement.domain.vo.InterimResultListVO;
import com.achievement.domain.vo.InterimResultStatsVO;
import com.achievement.domain.vo.InterimResultVO;
import com.achievement.mapper.InterimResultViewMapper;
import com.achievement.service.IInterimResultViewService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 中期成果物视图服务实现类
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterimResultViewServiceImpl extends ServiceImpl<InterimResultViewMapper, InterimResultView> 
        implements IInterimResultViewService {

    private final InterimResultViewMapper interimResultViewMapper;
    private final ObjectMapper objectMapper;

    @Override
    public InterimResultListVO getInterimResults(String projectId, String type, String projectName, 
                                               String submitter, String keyword, String year,
                                               Integer page, Integer pageSize) {
        log.info("查询中期成果物列表: projectId={}, type={}, page={}, pageSize={}", 
                projectId, type, page, pageSize);

        // 构建查询条件
        Long projectIdLong = null;
        if (StringUtils.hasText(projectId)) {
            try {
                projectIdLong = Long.valueOf(projectId);
            } catch (NumberFormatException e) {
                log.warn("无效的项目ID格式: {}", projectId);
            }
        }

        Integer yearValue = null;
        if (StringUtils.hasText(year)) {
            try {
                yearValue = Integer.valueOf(year);
            } catch (NumberFormatException e) {
                log.warn("无效的年份格式: {}", year);
            }
        }

        // 分页查询
        Page<InterimResultView> pageQuery = new Page<>(page, pageSize);
        Page<InterimResultView> pageResult = interimResultViewMapper.selectPageByConditions(
                pageQuery, projectIdLong, type, projectName, submitter, keyword, yearValue, null, null);

        // 转换为VO
        List<InterimResultVO> interimResults = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建响应
        InterimResultListVO response = new InterimResultListVO();
        response.setList(interimResults);
        response.setTotal(pageResult.getTotal());
        response.setPage(page);
        response.setPageSize(pageSize);

        log.info("查询中期成果物列表成功: total={}, page={}, pageSize={}", 
                pageResult.getTotal(), page, pageSize);

        return response;
    }

    @Override
    public List<InterimResultVO> listInterimResults(String projectId, String type, String projectName,
                                                   String submitter, String keyword, String year) {
        Long projectIdLong = null;
        if (StringUtils.hasText(projectId)) {
            try {
                projectIdLong = Long.valueOf(projectId);
            } catch (NumberFormatException e) {
                log.warn("无效的项目ID格式: {}", projectId);
            }
        }

        Integer yearValue = null;
        if (StringUtils.hasText(year)) {
            try {
                yearValue = Integer.valueOf(year);
            } catch (NumberFormatException e) {
                log.warn("无效的年份格式: {}", year);
            }
        }

        List<InterimResultView> results = interimResultViewMapper.selectByConditions(
                projectIdLong, type, projectName, submitter, keyword, yearValue, null, null);
        return results.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public InterimResultStatsVO getStats() {
        log.info("获取中期成果物统计数据");

        InterimResultStatsVO stats = new InterimResultStatsVO();

        // 统计项目总数
        Integer totalProjects = interimResultViewMapper.countTotalProjects();
        stats.setTotalProjects(totalProjects != null ? totalProjects : 0);

        // 统计成果物总数
        Integer totalResults = interimResultViewMapper.countTotalResults();
        stats.setTotalResults(totalResults != null ? totalResults : 0);

        // 按类型统计
        List<Map<String, Object>> typeStats = interimResultViewMapper.countByType();
        Map<String, Integer> byType = new HashMap<>();
        for (Map<String, Object> stat : typeStats) {
            String type = (String) stat.get("type");
            Long count = (Long) stat.get("count");
            byType.put(type, count.intValue());
        }
        stats.setByType(byType);

        // 按年份统计
        List<Map<String, Object>> yearStats = interimResultViewMapper.countByYear();
        Map<String, Integer> byYear = new HashMap<>();
        for (Map<String, Object> stat : yearStats) {
            Integer year = (Integer) stat.get("upload_year");
            Long count = (Long) stat.get("count");
            if (year != null) {
                byYear.put(String.valueOf(year), count.intValue());
            }
        }
        stats.setByYear(byYear);

        // 最近同步时间
        LocalDateTime recentSyncTime = interimResultViewMapper.getRecentSyncTime();
        stats.setRecentSyncTime(recentSyncTime);

        log.info("获取中期成果物统计数据成功: totalProjects={}, totalResults={}", 
                stats.getTotalProjects(), stats.getTotalResults());

        return stats;
    }

    @Override
    public InterimResultView getDetailById(String id) {
        log.info("根据ID获取中期成果物详情: id={}", id);

        try {
            Long idLong = Long.valueOf(id);
            InterimResultView result = interimResultViewMapper.selectById(idLong);
            
            if (result != null) {
                log.info("获取中期成果物详情成功: id={}, projectName={}", id, result.getProjectName());
            } else {
                log.warn("中期成果物不存在: id={}", id);
            }
            
            return result;
        } catch (NumberFormatException e) {
            log.warn("无效的成果物ID格式: {}", id);
            return null;
        }
    }

    @Override
    public InterimResultVO getDetailVO(String id) {
        InterimResultView view = getDetailById(id);
        return view == null ? null : convertToVO(view);
    }

    @Override
    public InterimResultListVO getInterimResultsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("根据时间范围查询中期成果物: startTime={}, endTime={}", startTime, endTime);

        List<InterimResultView> results = interimResultViewMapper.selectByTimeRange(startTime, endTime);

        // 转换为VO
        List<InterimResultVO> interimResults = results.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建响应
        InterimResultListVO response = new InterimResultListVO();
        response.setList(interimResults);
        response.setTotal((long) interimResults.size());
        response.setPage(1);
        response.setPageSize(interimResults.size());

        log.info("根据时间范围查询中期成果物成功: count={}", interimResults.size());

        return response;
    }

    /**
     * 将视图对象转换为VO
     */
    private InterimResultVO convertToVO(InterimResultView view) {
        InterimResultVO vo = new InterimResultVO();
        
        vo.setId(String.valueOf(view.getId()));
        vo.setProjectId(String.valueOf(view.getProjectId()));
        vo.setProjectName(view.getProjectName());
        vo.setProjectCode(view.getProjectCode());
        vo.setProjectPhase(view.getProjectPhase());
        vo.setName(view.getName());
        vo.setType(view.getType());
        vo.setTypeLabel(view.getTypeLabel());
        vo.setDescription(view.getDescription());
        vo.setSubmitter(view.getSubmitter());
        vo.setSubmitterDept(view.getSubmitterDept());
        vo.setSubmittedAt(view.getSubmittedAt());
        vo.setSyncedAt(view.getSyncedAt());
        vo.setSource(view.getSource());
        vo.setSourceRef(String.valueOf(view.getSourceRef()));
        vo.setSourceUrl(view.getSourceUrl());
        vo.setStatus(view.getStatus());

        // 解析标签JSON
        vo.setTags(parseTagsJson(view.getTags()));

        // 解析附件JSON
        vo.setAttachments(parseAttachmentsJson(view.getAttachmentsJson()));

        return vo;
    }

    /**
     * 解析标签JSON
     */
    private List<String> parseTagsJson(String tagsJson) {
        if (!StringUtils.hasText(tagsJson)) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("解析标签JSON失败: {}", tagsJson, e);
            return new ArrayList<>();
        }
    }

    /**
     * 解析附件JSON
     */
    private List<InterimResultVO.AttachmentVO> parseAttachmentsJson(String attachmentsJson) {
        if (!StringUtils.hasText(attachmentsJson)) {
            return new ArrayList<>();
        }

        try {
            List<Map<String, Object>> attachmentMaps = objectMapper.readValue(
                    attachmentsJson, new TypeReference<List<Map<String, Object>>>() {});

            return attachmentMaps.stream()
                    .map(this::convertToAttachmentVO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("解析附件JSON失败: {}", attachmentsJson, e);
            return new ArrayList<>();
        }
    }

    /**
     * 转换为附件VO
     */
    private InterimResultVO.AttachmentVO convertToAttachmentVO(Map<String, Object> attachmentMap) {
        InterimResultVO.AttachmentVO attachment = new InterimResultVO.AttachmentVO();
        
        String id = readString(attachmentMap, "id", "file_id", "fileId");
        String name = readString(attachmentMap, "name", "file_name", "fileName");
        String url = readString(attachmentMap, "url", "file_url", "fileUrl");
        String ext = readString(attachmentMap, "ext");
        String category = readString(attachmentMap, "category");

        attachment.setId(id);
        attachment.setName(name);
        attachment.setUrl(url);
        attachment.setExt(ext);
        attachment.setCategory(category);

        Long size = readLong(attachmentMap, "size", "file_size", "fileSize");
        if (size != null) {
            attachment.setSize(size);
        }

        return attachment;
    }

    private String readString(Map<String, Object> source, String... keys) {
        if (source == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object value = source.get(key);
            if (value instanceof String && StringUtils.hasText((String) value)) {
                return (String) value;
            }
        }
        return null;
    }

    private Long readLong(Map<String, Object> source, String... keys) {
        if (source == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object value = source.get(key);
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            if (value instanceof String && StringUtils.hasText((String) value)) {
                try {
                    return Long.parseLong((String) value);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }
}
