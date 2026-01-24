package com.achievement.service.impl;

import com.achievement.domain.dto.ProcessSubmissionQueryDTO;
import com.achievement.domain.dto.ProcessSubmissionRequest;
import com.achievement.domain.po.ProcessSubmission;
import com.achievement.domain.po.ProcessSubmissionFile;
import com.achievement.domain.vo.ProcessSubmissionVO;
import com.achievement.mapper.ProcessSubmissionFileMapper;
import com.achievement.mapper.ProcessSubmissionMapper;
import com.achievement.service.IProcessSystemService;
import com.achievement.service.ProcessSystemLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * 过程系统服务实现类
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessSystemServiceImpl extends ServiceImpl<ProcessSubmissionMapper, ProcessSubmission> implements IProcessSystemService {

    private final ProcessSubmissionMapper processSubmissionMapper;
    private final ProcessSubmissionFileMapper processSubmissionFileMapper;
    private final ProcessSystemLogService processSystemLogService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessSubmissionVO storeSubmission(ProcessSubmissionRequest request) {
        log.info("开始存储提交物，submissionId: {}, applicationId: {}", request.getSubmissionId(), request.getApplicationId());

        try {
            // 1. 验证数据
            if (!validateSubmissionData(request)) {
                processSystemLogService.logOperation("STORE_SUBMISSION", 
                    String.format("数据验证失败 - submissionId: %s, applicationId: %s", 
                        request.getSubmissionId(), request.getApplicationId()), false);
                throw new IllegalArgumentException("提交物数据验证失败");
            }

            // 2. 标准化数据
            request = normalizeSubmissionData(request);

            // 3. 检查重复
            if (isDuplicateSubmission(request)) {
                processSystemLogService.logOperation("STORE_SUBMISSION", 
                    String.format("重复提交检测 - submissionId: %s, applicationId: %s", 
                        request.getSubmissionId(), request.getApplicationId()), false);
                throw new IllegalArgumentException("提交物已存在，不允许重复提交");
            }

            // 4. 转换为实体对象
            ProcessSubmission submission = convertToEntity(request);
            submission.setSyncTime(LocalDateTime.now());

            // 5. 保存提交物主记录
            boolean saved = save(submission);
            if (!saved) {
                processSystemLogService.logOperation("STORE_SUBMISSION", 
                    String.format("保存提交物失败 - submissionId: %s, applicationId: %s", 
                        request.getSubmissionId(), request.getApplicationId()), false);
                throw new RuntimeException("保存提交物失败");
            }

            // 6. 保存文件记录
            saveSubmissionFiles(submission.getSubmissionId(), request);

            // 记录成功操作日志
            processSystemLogService.logOperation("STORE_SUBMISSION", 
                String.format("成功存储提交物 - submissionId: %s, applicationId: %s, projectName: %s", 
                    submission.getSubmissionId(), submission.getApplicationId(), submission.getProjectName()), true);

            log.info("提交物存储成功，submissionId: {}", submission.getSubmissionId());
            return convertToVO(submission);
            
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("STORE_SUBMISSION", 
                String.format("存储提交物异常 - submissionId: %s, applicationId: %s, error: %s", 
                    request.getSubmissionId(), request.getApplicationId(), e.getMessage()), false);
            throw e;
        }
    }

    @Override
    public ProcessSubmissionVO getSubmissionDetail(Long submissionId) {
        try {
            ProcessSubmission submission = getById(submissionId);
            if (submission == null) {
                processSystemLogService.logOperation("GET_SUBMISSION_DETAIL", 
                    String.format("提交物不存在 - submissionId: %s", submissionId), false);
                throw new IllegalArgumentException("提交物不存在");
            }
            
            // 记录成功操作日志
            processSystemLogService.logOperation("GET_SUBMISSION_DETAIL", 
                String.format("成功获取提交物详情 - submissionId: %s, projectName: %s", 
                    submissionId, submission.getProjectName()), true);
            
            return convertToVO(submission);
            
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("GET_SUBMISSION_DETAIL", 
                String.format("获取提交物详情异常 - submissionId: %s, error: %s", 
                    submissionId, e.getMessage()), false);
            throw e;
        }
    }

    @Override
    public List<ProcessSubmissionVO> getSubmissions(ProcessSubmissionQueryDTO queryDTO) {
        try {
            List<ProcessSubmission> submissions;
            
            if (queryDTO.getApplicationId() != null && StringUtils.hasText(queryDTO.getSubmissionStage())) {
                submissions = processSubmissionMapper.selectByApplicationIdAndStage(
                    queryDTO.getApplicationId(), queryDTO.getSubmissionStage());
            } else if (queryDTO.getApplicationId() != null) {
                submissions = processSubmissionMapper.selectByApplicationId(queryDTO.getApplicationId());
            } else if (queryDTO.getStartTime() != null && queryDTO.getEndTime() != null) {
                submissions = processSubmissionMapper.selectByTimeRange(queryDTO.getStartTime(), queryDTO.getEndTime());
            } else {
                // 使用通用查询
                LambdaQueryWrapper<ProcessSubmission> wrapper = buildQueryWrapper(queryDTO);
                submissions = list(wrapper);
            }

            // 记录成功操作日志
            processSystemLogService.logOperation("QUERY_SUBMISSIONS", 
                String.format("成功查询提交物列表 - applicationId: %s, stage: %s, count: %d", 
                    queryDTO.getApplicationId(), queryDTO.getSubmissionStage(), submissions.size()), true);

            return submissions.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("QUERY_SUBMISSIONS", 
                String.format("查询提交物列表异常 - applicationId: %s, stage: %s, error: %s", 
                    queryDTO.getApplicationId(), queryDTO.getSubmissionStage(), e.getMessage()), false);
            throw e;
        }
    }

    @Override
    public Page<ProcessSubmissionVO> getSubmissionsPage(ProcessSubmissionQueryDTO queryDTO) {
        try {
            Page<ProcessSubmission> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
            
            Page<ProcessSubmission> resultPage = processSubmissionMapper.selectPageByConditions(
                page,
                queryDTO.getApplicationId(),
                queryDTO.getSubmissionStage(),
                queryDTO.getSubmissionType(),
                queryDTO.getProjectName(),
                queryDTO.getApplicantName(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime()
            );

            Page<ProcessSubmissionVO> voPage = new Page<>();
            BeanUtils.copyProperties(resultPage, voPage, "records");
            
            List<ProcessSubmissionVO> voList = resultPage.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            voPage.setRecords(voList);

            // 记录成功操作日志
            processSystemLogService.logOperation("QUERY_SUBMISSIONS_PAGE", 
                String.format("成功分页查询提交物 - page: %d, size: %d, total: %d", 
                    queryDTO.getPageNum(), queryDTO.getPageSize(), resultPage.getTotal()), true);

            return voPage;
            
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("QUERY_SUBMISSIONS_PAGE", 
                String.format("分页查询提交物异常 - page: %d, size: %d, error: %s", 
                    queryDTO.getPageNum(), queryDTO.getPageSize(), e.getMessage()), false);
            throw e;
        }
    }

    @Override
    public List<ProcessSubmissionVO> getSubmissionsByApplicationId(Long applicationId) {
        try {
            List<ProcessSubmission> submissions = processSubmissionMapper.selectByApplicationId(applicationId);
            
            // 记录成功操作日志
            processSystemLogService.logOperation("QUERY_SUBMISSIONS_BY_APPLICATION", 
                String.format("成功按申报ID查询提交物 - applicationId: %s, count: %d", 
                    applicationId, submissions.size()), true);
            
            return submissions.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("QUERY_SUBMISSIONS_BY_APPLICATION", 
                String.format("按申报ID查询提交物异常 - applicationId: %s, error: %s", 
                    applicationId, e.getMessage()), false);
            throw e;
        }
    }

    @Override
    public List<ProcessSubmissionVO> getSubmissionsByApplicationIdAndStage(Long applicationId, String submissionStage) {
        try {
            List<ProcessSubmission> submissions = processSubmissionMapper.selectByApplicationIdAndStage(applicationId, submissionStage);
            
            // 记录成功操作日志
            processSystemLogService.logOperation("QUERY_SUBMISSIONS_BY_APPLICATION_AND_STAGE", 
                String.format("成功按申报ID和阶段查询提交物 - applicationId: %s, stage: %s, count: %d", 
                    applicationId, submissionStage, submissions.size()), true);
            
            return submissions.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("QUERY_SUBMISSIONS_BY_APPLICATION_AND_STAGE", 
                String.format("按申报ID和阶段查询提交物异常 - applicationId: %s, stage: %s, error: %s", 
                    applicationId, submissionStage, e.getMessage()), false);
            throw e;
        }
    }

    @Override
    public boolean validateSubmissionData(ProcessSubmissionRequest request) {
        // 基本字段验证
        if (request.getSubmissionId() == null || request.getApplicationId() == null) {
            log.warn("提交物ID或申报ID为空");
            return false;
        }

        if (!StringUtils.hasText(request.getSubmissionType()) || !StringUtils.hasText(request.getSubmissionStage())) {
            log.warn("提交物类型或提交阶段为空");
            return false;
        }

        // 项目信息验证
        ProcessSubmissionRequest.ProjectInfo projectInfo = request.getProjectInfo();
        if (projectInfo == null || !StringUtils.hasText(projectInfo.getProjectName()) || 
            !StringUtils.hasText(projectInfo.getCategoryLevel()) || !StringUtils.hasText(projectInfo.getCategorySpecific())) {
            log.warn("项目基本信息不完整");
            return false;
        }

        // 申报人信息验证
        ProcessSubmissionRequest.ApplicantInfo applicantInfo = request.getApplicantInfo();
        if (applicantInfo == null || !StringUtils.hasText(applicantInfo.getApplicantName()) || 
            !StringUtils.hasText(applicantInfo.getPhone())) {
            log.warn("申报人基本信息不完整");
            return false;
        }

        // 文件信息验证
        ProcessSubmissionRequest.FileInfo files = request.getFiles();
        if (files == null || files.getProposalFile() == null || 
            !StringUtils.hasText(files.getProposalFile().getFileId())) {
            log.warn("申报书文件信息不完整");
            return false;
        }

        // 上传信息验证
        ProcessSubmissionRequest.UploadInfo uploadInfo = request.getUploadInfo();
        if (uploadInfo == null || !StringUtils.hasText(uploadInfo.getUploaderId()) || 
            !StringUtils.hasText(uploadInfo.getUploaderName())) {
            log.warn("上传者信息不完整");
            return false;
        }

        return true;
    }

    @Override
    public ProcessSubmissionRequest normalizeSubmissionData(ProcessSubmissionRequest request) {
        // 标准化提交物类型
        if ("proposal".equalsIgnoreCase(request.getSubmissionType())) {
            request.setSubmissionType("proposal");
        } else if ("application_attachment".equalsIgnoreCase(request.getSubmissionType())) {
            request.setSubmissionType("application_attachment");
        }

        // 标准化提交阶段
        if ("application".equalsIgnoreCase(request.getSubmissionStage())) {
            request.setSubmissionStage("application");
        }

        // 标准化类别级别
        ProcessSubmissionRequest.ProjectInfo projectInfo = request.getProjectInfo();
        if (projectInfo != null) {
            if ("重点".equals(projectInfo.getCategoryLevel()) || "key".equalsIgnoreCase(projectInfo.getCategoryLevel())) {
                projectInfo.setCategoryLevel("重点");
            } else if ("一般".equals(projectInfo.getCategoryLevel()) || "general".equalsIgnoreCase(projectInfo.getCategoryLevel())) {
                projectInfo.setCategoryLevel("一般");
            }

            // 清理关键词格式
            if (StringUtils.hasText(projectInfo.getProjectKeywords())) {
                String keywords = projectInfo.getProjectKeywords().replaceAll("[，；;]", ",");
                projectInfo.setProjectKeywords(keywords);
            }
        }

        // 设置默认值
        if (request.getSubmissionRound() == null) {
            request.setSubmissionRound(1);
        }
        if (request.getSubmissionVersion() == null) {
            request.setSubmissionVersion(getNextVersion(
                request.getApplicationId(), 
                request.getSubmissionType(), 
                request.getSubmissionStage(), 
                request.getSubmissionRound()
            ));
        }

        return request;
    }

    @Override
    public boolean isDuplicateSubmission(ProcessSubmissionRequest request) {
        ProcessSubmission existing = processSubmissionMapper.selectDuplicate(
            request.getApplicationId(),
            request.getSubmissionType(),
            request.getSubmissionStage(),
            request.getSubmissionRound(),
            request.getSubmissionVersion()
        );
        return existing != null;
    }

    @Override
    public Integer getNextVersion(Long applicationId, String submissionType, String submissionStage, Integer submissionRound) {
        Integer maxVersion = processSubmissionMapper.selectMaxVersion(applicationId, submissionType, submissionStage, submissionRound);
        return maxVersion + 1;
    }

    @Override
    public Long countSubmissions(ProcessSubmissionQueryDTO queryDTO) {
        try {
            Long count = processSubmissionMapper.countByConditions(
                queryDTO.getApplicationId(),
                queryDTO.getSubmissionStage(),
                queryDTO.getSubmissionType()
            );
            
            // 记录成功操作日志
            processSystemLogService.logOperation("COUNT_SUBMISSIONS", 
                String.format("成功统计提交物数量 - applicationId: %s, stage: %s, type: %s, count: %d", 
                    queryDTO.getApplicationId(), queryDTO.getSubmissionStage(), 
                    queryDTO.getSubmissionType(), count), true);
            
            return count;
            
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("COUNT_SUBMISSIONS", 
                String.format("统计提交物数量异常 - applicationId: %s, stage: %s, type: %s, error: %s", 
                    queryDTO.getApplicationId(), queryDTO.getSubmissionStage(), 
                    queryDTO.getSubmissionType(), e.getMessage()), false);
            throw e;
        }
    }

    @Override
    public List<ProcessSubmissionVO> getSubmissionVersionHistory(Long applicationId, String submissionType, 
                                                               String submissionStage, Integer submissionRound) {
        try {
            List<ProcessSubmission> submissions = processSubmissionMapper.selectVersionHistory(
                applicationId, submissionType, submissionStage, submissionRound);
            
            // 记录成功操作日志
            processSystemLogService.logOperation("QUERY_VERSION_HISTORY", 
                String.format("成功查询版本历史 - applicationId: %s, type: %s, stage: %s, round: %d, count: %d", 
                    applicationId, submissionType, submissionStage, submissionRound, submissions.size()), true);
            
            return submissions.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("QUERY_VERSION_HISTORY", 
                String.format("查询版本历史异常 - applicationId: %s, type: %s, stage: %s, round: %d, error: %s", 
                    applicationId, submissionType, submissionStage, submissionRound, e.getMessage()), false);
            throw e;
        }
    }

    @Override
    public List<ProcessSubmissionVO> getSubmissionRoundHistory(Long applicationId, String submissionType, String submissionStage) {
        try {
            List<ProcessSubmission> submissions = processSubmissionMapper.selectRoundHistory(
                applicationId, submissionType, submissionStage);
            
            // 记录成功操作日志
            processSystemLogService.logOperation("QUERY_ROUND_HISTORY", 
                String.format("成功查询轮次历史 - applicationId: %s, type: %s, stage: %s, count: %d", 
                    applicationId, submissionType, submissionStage, submissions.size()), true);
            
            return submissions.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("QUERY_ROUND_HISTORY", 
                String.format("查询轮次历史异常 - applicationId: %s, type: %s, stage: %s, error: %s", 
                    applicationId, submissionType, submissionStage, e.getMessage()), false);
            throw e;
        }
    }

    @Override
    public ProcessSubmissionVO getSubmissionByVersion(Long applicationId, String submissionType, 
                                                    String submissionStage, Integer submissionRound, Integer submissionVersion) {
        try {
            ProcessSubmission submission = processSubmissionMapper.selectByVersion(
                applicationId, submissionType, submissionStage, submissionRound, submissionVersion);
            
            if (submission == null) {
                processSystemLogService.logOperation("GET_SUBMISSION_BY_VERSION", 
                    String.format("指定版本提交物不存在 - applicationId: %s, type: %s, stage: %s, round: %d, version: %d", 
                        applicationId, submissionType, submissionStage, submissionRound, submissionVersion), false);
                throw new IllegalArgumentException("指定版本的提交物不存在");
            }
            
            // 记录成功操作日志
            processSystemLogService.logOperation("GET_SUBMISSION_BY_VERSION", 
                String.format("成功获取指定版本提交物 - applicationId: %s, type: %s, stage: %s, round: %d, version: %d", 
                    applicationId, submissionType, submissionStage, submissionRound, submissionVersion), true);
            
            return convertToVO(submission);
            
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("GET_SUBMISSION_BY_VERSION", 
                String.format("获取指定版本提交物异常 - applicationId: %s, type: %s, stage: %s, round: %d, version: %d, error: %s", 
                    applicationId, submissionType, submissionStage, submissionRound, submissionVersion, e.getMessage()), false);
            throw e;
        }
    }

    @Override
    public List<ProcessSubmissionVO> getSubmissionFullHistory(Long applicationId, String submissionType, String submissionStage) {
        try {
            List<ProcessSubmission> submissions = processSubmissionMapper.selectFullHistory(
                applicationId, submissionType, submissionStage);
            
            // 记录成功操作日志
            processSystemLogService.logOperation("QUERY_FULL_HISTORY", 
                String.format("成功查询完整历史 - applicationId: %s, type: %s, stage: %s, count: %d", 
                    applicationId, submissionType, submissionStage, submissions.size()), true);
            
            return submissions.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("QUERY_FULL_HISTORY", 
                String.format("查询完整历史异常 - applicationId: %s, type: %s, stage: %s, error: %s", 
                    applicationId, submissionType, submissionStage, e.getMessage()), false);
            throw e;
        }
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<ProcessSubmission> buildQueryWrapper(ProcessSubmissionQueryDTO queryDTO) {
        LambdaQueryWrapper<ProcessSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessSubmission::getDelFlag, "0");
        
        if (queryDTO.getApplicationId() != null) {
            wrapper.eq(ProcessSubmission::getApplicationId, queryDTO.getApplicationId());
        }
        if (StringUtils.hasText(queryDTO.getSubmissionStage())) {
            wrapper.eq(ProcessSubmission::getSubmissionStage, queryDTO.getSubmissionStage());
        }
        if (StringUtils.hasText(queryDTO.getSubmissionType())) {
            wrapper.eq(ProcessSubmission::getSubmissionType, queryDTO.getSubmissionType());
        }
        if (StringUtils.hasText(queryDTO.getProjectName())) {
            wrapper.like(ProcessSubmission::getProjectName, queryDTO.getProjectName());
        }
        if (StringUtils.hasText(queryDTO.getApplicantName())) {
            wrapper.like(ProcessSubmission::getApplicantName, queryDTO.getApplicantName());
        }
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(ProcessSubmission::getUploadTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(ProcessSubmission::getUploadTime, queryDTO.getEndTime());
        }
        
        wrapper.orderByDesc(ProcessSubmission::getUploadTime);
        return wrapper;
    }

    /**
     * 转换请求为实体对象
     */
    private ProcessSubmission convertToEntity(ProcessSubmissionRequest request) {
        ProcessSubmission submission = new ProcessSubmission();
        
        // 基本信息
        submission.setSubmissionId(request.getSubmissionId());
        submission.setApplicationId(request.getApplicationId());
        submission.setSubmissionType(request.getSubmissionType());
        submission.setSubmissionStage(request.getSubmissionStage());
        submission.setSubmissionRound(request.getSubmissionRound());
        submission.setSubmissionVersion(request.getSubmissionVersion());

        // 项目信息
        ProcessSubmissionRequest.ProjectInfo projectInfo = request.getProjectInfo();
        submission.setProjectName(projectInfo.getProjectName());
        submission.setProjectField(projectInfo.getProjectField());
        submission.setCategoryLevel(projectInfo.getCategoryLevel());
        submission.setCategorySpecific(projectInfo.getCategorySpecific());
        submission.setResearchPeriod(projectInfo.getResearchPeriod());
        submission.setProjectKeywords(projectInfo.getProjectKeywords());
        submission.setProjectDescription(projectInfo.getProjectDescription());
        submission.setExpectedResults(projectInfo.getExpectedResults());
        submission.setWillingAdjust(projectInfo.getWillingAdjust());

        // 申报人信息
        ProcessSubmissionRequest.ApplicantInfo applicantInfo = request.getApplicantInfo();
        submission.setApplicantName(applicantInfo.getApplicantName());
        submission.setIdCard(applicantInfo.getIdCard());
        submission.setEducationDegree(applicantInfo.getEducationDegree());
        submission.setTechnicalTitle(applicantInfo.getTechnicalTitle());
        submission.setEmail(applicantInfo.getEmail());
        submission.setPhone(applicantInfo.getPhone());
        submission.setWorkUnit(applicantInfo.getWorkUnit());
        submission.setUnitAddress(applicantInfo.getUnitAddress());
        submission.setRepresentativeAchievements(applicantInfo.getRepresentativeAchievements());

        // 文件信息
        ProcessSubmissionRequest.FileInfo files = request.getFiles();
        ProcessSubmissionRequest.FileDetail proposalFile = files.getProposalFile();
        submission.setProposalFileId(proposalFile.getFileId());
        submission.setProposalFileName(proposalFile.getFileName());
        submission.setProposalFileSize(proposalFile.getFileSize());
        submission.setProposalFileType(proposalFile.getFileType());
        submission.setProposalFileUrl(proposalFile.getFileUrl());

        // 其他附件转JSON
        if (files.getOtherAttachments() != null && !files.getOtherAttachments().isEmpty()) {
            try {
                submission.setOtherAttachmentsJson(objectMapper.writeValueAsString(files.getOtherAttachments()));
            } catch (JsonProcessingException e) {
                log.error("转换附件信息为JSON失败", e);
                submission.setOtherAttachmentsJson("[]");
            }
        } else {
            submission.setOtherAttachmentsJson("[]");
        }

        // 上传信息
        ProcessSubmissionRequest.UploadInfo uploadInfo = request.getUploadInfo();
        submission.setUploaderId(uploadInfo.getUploaderId());
        submission.setUploaderName(uploadInfo.getUploaderName());
        LocalDateTime uploadTime = resolveUploadTime(uploadInfo.getUploadTime());
        submission.setUploadTime(uploadTime != null ? uploadTime : LocalDateTime.now());
        submission.setSubmissionDescription(uploadInfo.getSubmissionDescription());

        // 系统字段
        submission.setCreateBy(uploadInfo.getUploaderId());
        submission.setCreateTime(LocalDateTime.now());
        submission.setDelFlag("0");

        return submission;
    }

    /**
     * 转换实体为VO对象
     */
    private ProcessSubmissionVO convertToVO(ProcessSubmission submission) {
        ProcessSubmissionVO vo = new ProcessSubmissionVO();
        BeanUtils.copyProperties(submission, vo);

        // 转换申报书文件信息
        ProcessSubmissionVO.FileVO proposalFile = new ProcessSubmissionVO.FileVO();
        proposalFile.setFileId(submission.getProposalFileId());
        proposalFile.setFileName(submission.getProposalFileName());
        proposalFile.setFileSize(submission.getProposalFileSize());
        proposalFile.setFileType(submission.getProposalFileType());
        proposalFile.setFileUrl(submission.getProposalFileUrl());
        vo.setProposalFile(proposalFile);

        // 转换其他附件信息
        if (StringUtils.hasText(submission.getOtherAttachmentsJson())) {
            try {
                List<ProcessSubmissionRequest.FileDetail> attachments = objectMapper.readValue(
                    submission.getOtherAttachmentsJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ProcessSubmissionRequest.FileDetail.class)
                );
                List<ProcessSubmissionVO.FileVO> attachmentVOs = attachments.stream()
                    .map(attachment -> {
                        ProcessSubmissionVO.FileVO fileVO = new ProcessSubmissionVO.FileVO();
                        BeanUtils.copyProperties(attachment, fileVO);
                        return fileVO;
                    })
                    .collect(Collectors.toList());
                vo.setOtherAttachments(attachmentVOs);
            } catch (JsonProcessingException e) {
                log.error("解析附件信息JSON失败", e);
                vo.setOtherAttachments(new ArrayList<>());
            }
        } else {
            vo.setOtherAttachments(new ArrayList<>());
        }

        return vo;
    }

    /**
     * 保存提交物文件记录
     */
    private void saveSubmissionFiles(Long submissionId, ProcessSubmissionRequest request) {
        try {
            List<ProcessSubmissionFile> files = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            // 申报书文件
            ProcessSubmissionRequest.FileDetail proposalFile = request.getFiles().getProposalFile();
            ProcessSubmissionFile proposalFileEntity = new ProcessSubmissionFile();
            proposalFileEntity.setFileId(proposalFile.getFileId());
            proposalFileEntity.setSubmissionId(submissionId);
            proposalFileEntity.setFileName(proposalFile.getFileName());
            proposalFileEntity.setOriginalName(proposalFile.getFileName());
            proposalFileEntity.setFileSize(proposalFile.getFileSize());
            proposalFileEntity.setFileType(proposalFile.getFileType());
            proposalFileEntity.setFileUrl(proposalFile.getFileUrl());
            proposalFileEntity.setFileCategory("proposal");
            proposalFileEntity.setStorageStatus("completed");
            proposalFileEntity.setUploaderId(request.getUploadInfo().getUploaderId());
            proposalFileEntity.setUploaderName(request.getUploadInfo().getUploaderName());
            proposalFileEntity.setUploadTime(now);
            proposalFileEntity.setCreateBy(request.getUploadInfo().getUploaderId());
            proposalFileEntity.setCreateTime(now);
            proposalFileEntity.setDelFlag("0");
            files.add(proposalFileEntity);

            // 其他附件
            if (request.getFiles().getOtherAttachments() != null) {
                for (ProcessSubmissionRequest.FileDetail attachment : request.getFiles().getOtherAttachments()) {
                    ProcessSubmissionFile attachmentEntity = new ProcessSubmissionFile();
                    attachmentEntity.setFileId(attachment.getFileId());
                    attachmentEntity.setSubmissionId(submissionId);
                    attachmentEntity.setFileName(attachment.getFileName());
                    attachmentEntity.setOriginalName(attachment.getFileName());
                    attachmentEntity.setFileSize(attachment.getFileSize());
                    attachmentEntity.setFileType(attachment.getFileType());
                    attachmentEntity.setFileUrl(attachment.getFileUrl());
                    attachmentEntity.setFileCategory("attachment");
                    attachmentEntity.setStorageStatus("completed");
                    attachmentEntity.setUploaderId(request.getUploadInfo().getUploaderId());
                    attachmentEntity.setUploaderName(request.getUploadInfo().getUploaderName());
                    attachmentEntity.setUploadTime(now);
                    attachmentEntity.setCreateBy(request.getUploadInfo().getUploaderId());
                    attachmentEntity.setCreateTime(now);
                    attachmentEntity.setDelFlag("0");
                    files.add(attachmentEntity);
                }
            }

            // 批量保存文件记录
            if (!files.isEmpty()) {
                processSubmissionFileMapper.batchInsert(files);
                
                // 记录文件保存操作日志
                processSystemLogService.logOperation("SAVE_SUBMISSION_FILES", 
                    String.format("成功保存提交物文件记录 - submissionId: %s, fileCount: %d", 
                        submissionId, files.size()), true);
            }
            
        } catch (Exception e) {
            // 记录文件保存失败日志
            processSystemLogService.logOperation("SAVE_SUBMISSION_FILES", 
                String.format("保存提交物文件记录失败 - submissionId: %s, error: %s", 
                    submissionId, e.getMessage()), false);
            throw e;
        }
    }

    private LocalDateTime resolveUploadTime(String uploadTime) {
        if (!StringUtils.hasText(uploadTime)) {
            return null;
        }

        String value = uploadTime.trim();
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        try {
            return java.time.LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        } catch (DateTimeParseException ignored) {
        }

        return null;
    }

    // ==================== 文件信息查询相关方法实现 ====================

    @Override
    public List<ProcessSubmissionFile> getFiles(Long submissionId, String fileCategory, String fileName, String fileType) {
        try {
            LambdaQueryWrapper<ProcessSubmissionFile> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProcessSubmissionFile::getDelFlag, "0");
            
            if (submissionId != null) {
                wrapper.eq(ProcessSubmissionFile::getSubmissionId, submissionId);
            }
            if (StringUtils.hasText(fileCategory)) {
                wrapper.eq(ProcessSubmissionFile::getFileCategory, fileCategory);
            }
            if (StringUtils.hasText(fileName)) {
                wrapper.like(ProcessSubmissionFile::getFileName, fileName);
            }
            if (StringUtils.hasText(fileType)) {
                wrapper.eq(ProcessSubmissionFile::getFileType, fileType);
            }
            
            wrapper.orderByDesc(ProcessSubmissionFile::getUploadTime);
            
            List<ProcessSubmissionFile> files = processSubmissionFileMapper.selectList(wrapper);
            
            // 记录成功操作日志
            processSystemLogService.logOperation("QUERY_FILES", 
                String.format("成功查询文件列表 - submissionId: %s, fileCategory: %s, fileName: %s, fileType: %s, count: %d", 
                    submissionId, fileCategory, fileName, fileType, files.size()), true);
            
            return files;
            
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("QUERY_FILES", 
                String.format("查询文件列表异常 - submissionId: %s, fileCategory: %s, fileName: %s, fileType: %s, error: %s", 
                    submissionId, fileCategory, fileName, fileType, e.getMessage()), false);
            throw e;
        }
    }

    @Override
    public Long countFiles(Long submissionId, String fileCategory, String fileName, String fileType) {
        try {
            LambdaQueryWrapper<ProcessSubmissionFile> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProcessSubmissionFile::getDelFlag, "0");
            
            if (submissionId != null) {
                wrapper.eq(ProcessSubmissionFile::getSubmissionId, submissionId);
            }
            if (StringUtils.hasText(fileCategory)) {
                wrapper.eq(ProcessSubmissionFile::getFileCategory, fileCategory);
            }
            if (StringUtils.hasText(fileName)) {
                wrapper.like(ProcessSubmissionFile::getFileName, fileName);
            }
            if (StringUtils.hasText(fileType)) {
                wrapper.eq(ProcessSubmissionFile::getFileType, fileType);
            }
            
            Long count = processSubmissionFileMapper.selectCount(wrapper);
            
            // 记录成功操作日志
            processSystemLogService.logOperation("COUNT_FILES", 
                String.format("成功统计文件数量 - submissionId: %s, fileCategory: %s, fileName: %s, fileType: %s, count: %d", 
                    submissionId, fileCategory, fileName, fileType, count), true);
            
            return count;
            
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("COUNT_FILES", 
                String.format("统计文件数量异常 - submissionId: %s, fileCategory: %s, fileName: %s, fileType: %s, error: %s", 
                    submissionId, fileCategory, fileName, fileType, e.getMessage()), false);
            throw e;
        }
    }

    @Override
    public ProcessSubmissionFile getFileMetadata(String fileId) {
        try {
            if (!StringUtils.hasText(fileId)) {
                throw new IllegalArgumentException("文件ID不能为空");
            }
            
            ProcessSubmissionFile file = processSubmissionFileMapper.selectByFileId(fileId);
            
            if (file == null) {
                processSystemLogService.logOperation("GET_FILE_METADATA", 
                    String.format("文件不存在 - fileId: %s", fileId), false);
                return null;
            }
            
            // 记录成功操作日志
            processSystemLogService.logOperation("GET_FILE_METADATA", 
                String.format("成功获取文件元数据 - fileId: %s, fileName: %s", fileId, file.getFileName()), true);
            
            return file;
            
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("GET_FILE_METADATA", 
                String.format("获取文件元数据异常 - fileId: %s, error: %s", fileId, e.getMessage()), false);
            throw e;
        }
    }

    @Override
    public List<ProcessSubmissionFile> getFilesBySubmissionId(Long submissionId, String fileCategory) {
        try {
            if (submissionId == null) {
                throw new IllegalArgumentException("提交物ID不能为空");
            }
            
            List<ProcessSubmissionFile> files;
            if (StringUtils.hasText(fileCategory)) {
                files = processSubmissionFileMapper.selectBySubmissionIdAndCategory(submissionId, fileCategory);
            } else {
                files = processSubmissionFileMapper.selectBySubmissionId(submissionId);
            }
            
            // 记录成功操作日志
            processSystemLogService.logOperation("GET_FILES_BY_SUBMISSION", 
                String.format("成功根据提交物ID查询文件 - submissionId: %s, fileCategory: %s, count: %d", 
                    submissionId, fileCategory, files.size()), true);
            
            return files;
            
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("GET_FILES_BY_SUBMISSION", 
                String.format("根据提交物ID查询文件异常 - submissionId: %s, fileCategory: %s, error: %s", 
                    submissionId, fileCategory, e.getMessage()), false);
            throw e;
        }
    }

    @Override
    public String getFileDownloadUrl(String fileId) {
        try {
            if (!StringUtils.hasText(fileId)) {
                throw new IllegalArgumentException("文件ID不能为空");
            }
            
            ProcessSubmissionFile file = processSubmissionFileMapper.selectByFileId(fileId);
            if (file == null) {
                processSystemLogService.logOperation("GET_FILE_DOWNLOAD_URL", 
                    String.format("文件不存在 - fileId: %s", fileId), false);
                throw new IllegalArgumentException("文件不存在或无权限访问");
            }
            
            // 生成临时下载链接（这里简化处理，实际应该生成带签名的临时链接）
            String downloadUrl = file.getFileUrl();
            if (!downloadUrl.startsWith("http")) {
                // 如果是相对路径，转换为完整URL
                downloadUrl = "http://localhost:8080" + (downloadUrl.startsWith("/") ? "" : "/") + downloadUrl;
            }
            
            // 记录成功操作日志
            processSystemLogService.logOperation("GET_FILE_DOWNLOAD_URL", 
                String.format("成功获取文件下载链接 - fileId: %s, fileName: %s", fileId, file.getFileName()), true);
            
            return downloadUrl;
            
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("GET_FILE_DOWNLOAD_URL", 
                String.format("获取文件下载链接异常 - fileId: %s, error: %s", fileId, e.getMessage()), false);
            throw e;
        }
    }

    @Override
    public Map<String, String> getBatchFileDownloadUrls(List<String> fileIds) {
        try {
            if (fileIds == null || fileIds.isEmpty()) {
                throw new IllegalArgumentException("文件ID列表不能为空");
            }
            
            Map<String, String> downloadUrls = new HashMap<>();
            
            for (String fileId : fileIds) {
                try {
                    ProcessSubmissionFile file = processSubmissionFileMapper.selectByFileId(fileId);
                    if (file != null) {
                        String downloadUrl = file.getFileUrl();
                        if (!downloadUrl.startsWith("http")) {
                            // 如果是相对路径，转换为完整URL
                            downloadUrl = "http://localhost:8080" + (downloadUrl.startsWith("/") ? "" : "/") + downloadUrl;
                        }
                        downloadUrls.put(fileId, downloadUrl);
                    } else {
                        log.warn("批量获取下载链接时文件不存在: fileId={}", fileId);
                    }
                } catch (Exception e) {
                    log.error("批量获取下载链接时处理文件异常: fileId={}, error={}", fileId, e.getMessage());
                }
            }
            
            // 记录成功操作日志
            processSystemLogService.logOperation("GET_BATCH_FILE_DOWNLOAD_URLS", 
                String.format("成功批量获取文件下载链接 - requestCount: %d, successCount: %d", 
                    fileIds.size(), downloadUrls.size()), true);
            
            return downloadUrls;
            
        } catch (Exception e) {
            // 记录失败操作日志
            processSystemLogService.logOperation("GET_BATCH_FILE_DOWNLOAD_URLS", 
                String.format("批量获取文件下载链接异常 - requestCount: %d, error: %s", 
                    fileIds != null ? fileIds.size() : 0, e.getMessage()), false);
            throw e;
        }
    }
}
