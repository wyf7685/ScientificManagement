package com.achievement.service;

import com.achievement.domain.po.ProcessSubmission;
import com.achievement.domain.vo.InterimResultVO;
import com.achievement.domain.vo.InterimResultStatsVO;

import java.util.List;

/**
 * 数据转换服务接口
 */
public interface IDataTransformationService {

    /**
     * 将ProcessSubmission转换为InterimResultVO
     *
     * @param submission 过程系统提交物
     * @return 中期成果物VO
     */
    InterimResultVO transformToInterimResult(ProcessSubmission submission);

    /**
     * 批量转换ProcessSubmission为InterimResultVO
     *
     * @param submissions 过程系统提交物列表
     * @return 中期成果物VO列表
     */
    List<InterimResultVO> transformToInterimResults(List<ProcessSubmission> submissions);

    /**
     * 生成统计数据
     *
     * @param submissions 过程系统提交物列表
     * @return 统计数据VO
     */
    InterimResultStatsVO generateStats(List<ProcessSubmission> submissions);

    /**
     * 映射提交物类型为前端类型
     *
     * @param submissionType 提交物类型
     * @return 前端类型
     */
    String mapSubmissionType(String submissionType);

    /**
     * 映射提交阶段为前端阶段
     *
     * @param submissionStage 提交阶段
     * @return 前端阶段
     */
    String mapSubmissionStage(String submissionStage);

    /**
     * 生成项目编码
     *
     * @param applicationId 申报ID
     * @return 项目编码
     */
    String generateProjectCode(Long applicationId);

    /**
     * 生成标签列表
     *
     * @param submission 过程系统提交物
     * @return 标签列表
     */
    List<String> generateTags(ProcessSubmission submission);
}