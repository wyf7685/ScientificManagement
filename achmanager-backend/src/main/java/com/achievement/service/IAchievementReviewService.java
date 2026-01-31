package com.achievement.service;

import com.achievement.domain.dto.AssignReviewerDTO;
import com.achievement.domain.dto.KeycloakUser;
import com.achievement.domain.dto.ReviewRequestDTO;
import com.achievement.domain.vo.ReviewBacklogVO;
import com.achievement.domain.vo.ReviewHistoryVO;
import com.achievement.domain.vo.ReviewResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 成果审核服务接口
 */
public interface IAchievementReviewService {
    
    /**
     * 提交成果进入审核流程
     * @param achievementDocId 成果documentId
     * @param userId 当前用户ID
     * @return 审核结果
     */
    ReviewResultVO submitForReview(String achievementDocId, Integer userId);
    
    /**
     * 审核成果
     * @param achievementDocId 成果documentId
     * @param reviewRequest 审核请求
     * @param reviewer 审核人
     * @return 审核结果
     */
    ReviewResultVO reviewAchievement(String achievementDocId, ReviewRequestDTO reviewRequest, KeycloakUser reviewer);
    
    /**
     * 分配审核人
     * @param achievementDocId 成果documentId
     * @param assignReviewerDTO 分配审核人请求
     * @param assigner 分配人
     * @return 分配结果
     */
    ReviewResultVO assignReviewers(String achievementDocId, AssignReviewerDTO assignReviewerDTO, KeycloakUser assigner);

    /**
     * 获取审核待办列表
     * @param reviewerId 审核人ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页待办列表
     */
    Page<ReviewBacklogVO> pageReviewBacklog(Integer reviewerId, Integer pageNum, Integer pageSize);

    /**
     * 获取审核历史列表
     * @param reviewerId 审核人ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页历史列表
     */
    Page<ReviewHistoryVO> pageReviewHistory(Integer reviewerId, Integer pageNum, Integer pageSize);
}
