package com.achievement.mapper;

import com.achievement.domain.po.ProcessSubmission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 过程系统项目提交物表 Mapper 接口
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
public interface ProcessSubmissionMapper extends BaseMapper<ProcessSubmission> {

    /**
     * 根据申报ID查询提交物列表
     */
    List<ProcessSubmission> selectByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 根据申报ID和提交阶段查询提交物列表
     */
    List<ProcessSubmission> selectByApplicationIdAndStage(@Param("applicationId") Long applicationId, 
                                                         @Param("submissionStage") String submissionStage);

    /**
     * 根据时间范围查询提交物列表
     */
    List<ProcessSubmission> selectByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 分页查询提交物列表
     */
    Page<ProcessSubmission> selectPageByConditions(Page<?> page, 
                                                  @Param("applicationId") Long applicationId,
                                                  @Param("submissionStage") String submissionStage,
                                                  @Param("submissionType") String submissionType,
                                                  @Param("projectName") String projectName,
                                                  @Param("applicantName") String applicantName,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 检查是否存在重复的提交物
     */
    ProcessSubmission selectDuplicate(@Param("applicationId") Long applicationId,
                                    @Param("submissionType") String submissionType,
                                    @Param("submissionStage") String submissionStage,
                                    @Param("submissionRound") Integer submissionRound,
                                    @Param("submissionVersion") Integer submissionVersion);

    /**
     * 获取最新版本号
     */
    Integer selectMaxVersion(@Param("applicationId") Long applicationId,
                           @Param("submissionType") String submissionType,
                           @Param("submissionStage") String submissionStage,
                           @Param("submissionRound") Integer submissionRound);

    /**
     * 统计提交物数量
     */
    Long countByConditions(@Param("applicationId") Long applicationId,
                          @Param("submissionStage") String submissionStage,
                          @Param("submissionType") String submissionType);

    /**
     * 根据版本号查询提交物历史记录
     */
    List<ProcessSubmission> selectVersionHistory(@Param("applicationId") Long applicationId,
                                               @Param("submissionType") String submissionType,
                                               @Param("submissionStage") String submissionStage,
                                               @Param("submissionRound") Integer submissionRound);

    /**
     * 根据轮次查询提交物历史记录
     */
    List<ProcessSubmission> selectRoundHistory(@Param("applicationId") Long applicationId,
                                             @Param("submissionType") String submissionType,
                                             @Param("submissionStage") String submissionStage);

    /**
     * 获取指定版本的提交物
     */
    ProcessSubmission selectByVersion(@Param("applicationId") Long applicationId,
                                    @Param("submissionType") String submissionType,
                                    @Param("submissionStage") String submissionStage,
                                    @Param("submissionRound") Integer submissionRound,
                                    @Param("submissionVersion") Integer submissionVersion);

    /**
     * 获取提交物的完整历史记录
     */
    List<ProcessSubmission> selectFullHistory(@Param("applicationId") Long applicationId,
                                            @Param("submissionType") String submissionType,
                                            @Param("submissionStage") String submissionStage);
}