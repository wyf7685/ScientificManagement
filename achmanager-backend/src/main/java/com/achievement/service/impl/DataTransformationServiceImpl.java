package com.achievement.service.impl;

import com.achievement.domain.po.ProcessSubmission;
import com.achievement.domain.vo.InterimResultVO;
import com.achievement.domain.vo.InterimResultStatsVO;
import com.achievement.service.IDataTransformationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据转换服务实现
 */
@Slf4j
@Service
public class DataTransformationServiceImpl implements IDataTransformationService {

    @Autowired
    private ObjectMapper objectMapper;

    // 提交物类型映射
    private static final Map<String, String> TYPE_MAPPING = Map.of(
            "proposal", "application",
            "application_attachment", "other"
    );

    // 提交物类型标签映射
    private static final Map<String, String> TYPE_LABEL_MAPPING = Map.of(
            "proposal", "申报书",
            "application_attachment", "其他附件"
    );

    // 项目阶段映射
    private static final Map<String, String> PHASE_MAPPING = Map.of(
            "application", "申报阶段",
            "review", "评审阶段",
            "execution", "执行阶段"
    );

    @Override
    public InterimResultVO transformToInterimResult(ProcessSubmission submission) {
        if (submission == null) {
            return null;
        }

        InterimResultVO result = new InterimResultVO();
        
        // 基本信息
        result.setId(String.valueOf(submission.getSubmissionId()));
        result.setProjectId(String.valueOf(submission.getApplicationId()));
        result.setProjectName(submission.getProjectName());
        result.setProjectCode(generateProjectCode(submission.getApplicationId()));
        result.setProjectPhase(mapSubmissionStage(submission.getSubmissionStage()));
        
        // 成果物信息
        result.setName(StringUtils.hasText(submission.getProjectDescription()) ? 
                submission.getProjectDescription() : submission.getProjectName());
        result.setType(mapSubmissionType(submission.getSubmissionType()));
        result.setTypeLabel(TYPE_LABEL_MAPPING.getOrDefault(submission.getSubmissionType(), "其他"));
        result.setDescription(submission.getProjectDescription());
        
        // 附件信息
        result.setAttachments(buildAttachments(submission));
        
        // 提交者信息
        String submitterName = StringUtils.hasText(submission.getUploaderName())
                ? submission.getUploaderName()
                : submission.getApplicantName();
        result.setSubmitter(submitterName);
        result.setSubmitterDept(submission.getWorkUnit());
        result.setSubmittedAt(submission.getUploadTime());
        result.setSyncedAt(submission.getSyncTime());
        
        // 来源信息
        result.setSource("process_system");
        result.setSourceRef(String.valueOf(submission.getSubmissionId()));
        result.setSourceUrl("http://process.example.com/submissions/" + submission.getSubmissionId());
        
        // 标签和状态
        result.setTags(generateTags(submission));
        result.setStatus("synced");
        
        return result;
    }

    @Override
    public List<InterimResultVO> transformToInterimResults(List<ProcessSubmission> submissions) {
        if (submissions == null || submissions.isEmpty()) {
            return new ArrayList<>();
        }
        
        return submissions.stream()
                .map(this::transformToInterimResult)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public InterimResultStatsVO generateStats(List<ProcessSubmission> submissions) {
        InterimResultStatsVO stats = new InterimResultStatsVO();
        
        if (submissions == null || submissions.isEmpty()) {
            stats.setTotalProjects(0);
            stats.setTotalResults(0);
            stats.setByType(new HashMap<>());
            stats.setByYear(new HashMap<>());
            return stats;
        }
        
        // 统计项目总数（按applicationId去重）
        Set<Long> uniqueProjects = submissions.stream()
                .map(ProcessSubmission::getApplicationId)
                .collect(Collectors.toSet());
        stats.setTotalProjects(uniqueProjects.size());
        
        // 统计成果物总数
        stats.setTotalResults(submissions.size());
        
        // 按类型统计
        Map<String, Integer> byType = submissions.stream()
                .collect(Collectors.groupingBy(
                        s -> mapSubmissionType(s.getSubmissionType()),
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));
        stats.setByType(byType);
        
        // 按年份统计
        Map<String, Integer> byYear = submissions.stream()
                .filter(s -> s.getUploadTime() != null)
                .collect(Collectors.groupingBy(
                        s -> String.valueOf(s.getUploadTime().getYear()),
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));
        stats.setByYear(byYear);
        
        // 最近同步时间
        Optional<LocalDateTime> recentSyncTime = submissions.stream()
                .map(ProcessSubmission::getSyncTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo);
        recentSyncTime.ifPresent(stats::setRecentSyncTime);
        
        return stats;
    }

    @Override
    public String mapSubmissionType(String submissionType) {
        return TYPE_MAPPING.getOrDefault(submissionType, "other");
    }

    @Override
    public String mapSubmissionStage(String submissionStage) {
        return PHASE_MAPPING.getOrDefault(submissionStage, submissionStage);
    }

    @Override
    public String generateProjectCode(Long applicationId) {
        if (applicationId == null) {
            return "";
        }
        return String.format("PROC%06d", applicationId);
    }

    @Override
    public List<String> generateTags(ProcessSubmission submission) {
        List<String> tags = new ArrayList<>();
        
        // 根据类别级别添加标签
        if (StringUtils.hasText(submission.getCategoryLevel())) {
            if ("重点".equals(submission.getCategoryLevel())) {
                tags.add("重点项目");
            } else if ("一般".equals(submission.getCategoryLevel())) {
                tags.add("一般项目");
            }
        }
        
        // 根据项目领域添加标签
        if (StringUtils.hasText(submission.getProjectField())) {
            tags.add(submission.getProjectField());
        }
        
        return tags;
    }

    /**
     * 构建附件列表
     */
    private List<InterimResultVO.AttachmentVO> buildAttachments(ProcessSubmission submission) {
        List<InterimResultVO.AttachmentVO> attachments = new ArrayList<>();
        
        // 添加申报书文件
        if (StringUtils.hasText(submission.getProposalFileId())) {
            InterimResultVO.AttachmentVO proposalFile = new InterimResultVO.AttachmentVO();
            proposalFile.setId(submission.getProposalFileId());
            proposalFile.setName(submission.getProposalFileName());
            proposalFile.setUrl(submission.getProposalFileUrl());
            proposalFile.setSize(submission.getProposalFileSize());
            proposalFile.setExt(getFileExtension(submission.getProposalFileName()));
            proposalFile.setCategory("proposal");
            attachments.add(proposalFile);
        }
        
        // 添加其他附件
        if (StringUtils.hasText(submission.getOtherAttachmentsJson())) {
            try {
                List<Map<String, Object>> otherAttachments = objectMapper.readValue(
                        submission.getOtherAttachmentsJson(),
                        new TypeReference<List<Map<String, Object>>>() {}
                );
                
                for (Map<String, Object> attachment : otherAttachments) {
                    InterimResultVO.AttachmentVO attachmentVO = new InterimResultVO.AttachmentVO();
                    String id = readString(attachment, "file_id", "fileId", "id");
                    String name = readString(attachment, "file_name", "fileName", "name");
                    String url = readString(attachment, "file_url", "fileUrl", "url");
                    String ext = readString(attachment, "ext");
                    String category = readString(attachment, "category");

                    attachmentVO.setId(id);
                    attachmentVO.setName(name);
                    attachmentVO.setUrl(url);
                    
                    Long size = readLong(attachment, "file_size", "fileSize", "size");
                    if (size != null) {
                        attachmentVO.setSize(size);
                    }
                    
                    String fileName = StringUtils.hasText(name) ? name : null;
                    attachmentVO.setExt(StringUtils.hasText(ext) ? ext : getFileExtension(fileName));
                    attachmentVO.setCategory(StringUtils.hasText(category) ? category : "attachment");
                    attachments.add(attachmentVO);
                }
            } catch (Exception e) {
                log.warn("Failed to parse other attachments JSON for submission {}: {}", 
                        submission.getSubmissionId(), e.getMessage());
            }
        }
        
        return attachments;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        
        return "";
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
