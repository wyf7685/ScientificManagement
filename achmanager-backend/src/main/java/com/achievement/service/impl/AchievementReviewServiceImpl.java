package com.achievement.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.achievement.domain.dto.AssignReviewerDTO;
import com.achievement.domain.dto.KeycloakUser;
import com.achievement.domain.dto.ReviewRequestDTO;
import com.achievement.domain.po.AchievementMains;
import com.achievement.domain.po.AchievementReview;
import com.achievement.domain.po.AchievementReviewerAssignment;
import com.achievement.domain.vo.ReviewBacklogVO;
import com.achievement.domain.vo.ReviewHistoryVO;
import com.achievement.domain.vo.ReviewResultVO;
import com.achievement.mapper.AchievementMainsMapper;
import com.achievement.mapper.AchievementReviewMapper;
import com.achievement.mapper.AchievementReviewerAssignmentMapper;
import com.achievement.service.IAchievementReviewService;
import com.achievement.service.IKeycloakUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 成果审核服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementReviewServiceImpl implements IAchievementReviewService {

    private final AchievementMainsMapper achievementMainsMapper;
    private final AchievementReviewMapper achievementReviewMapper;
    private final AchievementReviewerAssignmentMapper assignmentMapper;
    private final IKeycloakUserService keycloakUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResultVO submitForReview(String achievementDocId, Integer userId) {
        log.info("提交审核: achievementDocId={}, userId={}", achievementDocId, userId);

        // 查询成果
        AchievementMains achievement = getAchievementByDocId(achievementDocId);
        if (achievement == null) {
            throw new RuntimeException("成果不存在");
        }

        // 检查状态
        String currentStatus = achievement.getAchievementStatus();
        if (!"PENDING".equals(currentStatus) && !"NEEDS_MODIFICATION".equals(currentStatus)) {
            throw new RuntimeException("当前状态不允许提交审核");
        }

        // 更新状态为审核中
        LambdaUpdateWrapper<AchievementMains> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AchievementMains::getDocumentId, achievementDocId)
                .set(AchievementMains::getAchievementStatus, "UNDER_REVIEW")
                .set(AchievementMains::getUpdatedAt, LocalDateTime.now());

        achievementMainsMapper.update(null, updateWrapper);

        // 构建返回结果
        ReviewResultVO result = new ReviewResultVO();
        result.setAchievementDocId(achievementDocId);
        result.setStatus("UNDER_REVIEW");
        result.setSuccess(true);
        result.setMessage("已提交审核");

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResultVO reviewAchievement(String achievementDocId, ReviewRequestDTO reviewRequest,
            KeycloakUser reviewer) {
        log.info("审核成果: achievementDocId={}, action={}, reviewerId={}",
                achievementDocId, reviewRequest.getAction(), reviewer.getUuid());

        // 查询成果
        AchievementMains achievement = getAchievementByDocId(achievementDocId);
        if (achievement == null) {
            throw new RuntimeException("成果不存在");
        }

        // 检查状态
        if (!"UNDER_REVIEW".equals(achievement.getAchievementStatus())) {
            throw new RuntimeException("成果不在审核状态");
        }

        // 根据审核动作确定新状态
        String previousStatus = achievement.getAchievementStatus();
        String newStatus;
        String message;
        switch (reviewRequest.getAction().toLowerCase()) {
            case "approve":
                newStatus = "APPROVED";
                message = "审核通过";
                break;
            case "reject":
                newStatus = "REJECTED";
                message = "审核驳回";
                break;
            case "request_changes":
                newStatus = "NEEDS_MODIFICATION";
                message = "要求修改";
                break;
            default:
                throw new RuntimeException("无效的审核动作: " + reviewRequest.getAction());
        }

        String reviewerName = reviewer.getName();

        // 更新成果状态和审核信息
        LambdaUpdateWrapper<AchievementMains> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AchievementMains::getDocumentId, achievementDocId)
                .set(AchievementMains::getAchievementStatus, newStatus)
                .set(AchievementMains::getReviewerId, reviewer.getUuid())
                .set(AchievementMains::getReviewerName, reviewerName)
                .set(AchievementMains::getReviewComment, reviewRequest.getComment())
                .set(AchievementMains::getReviewedAt, LocalDateTime.now())
                .set(AchievementMains::getUpdatedAt, LocalDateTime.now());

        // 如果通过且设置了可见范围
        if ("approve".equalsIgnoreCase(reviewRequest.getAction())
                && reviewRequest.getVisibilityRange() != null) {
            updateWrapper.set(AchievementMains::getVisibilityRange, reviewRequest.getVisibilityRange());
        }

        achievementMainsMapper.update(null, updateWrapper);

        LocalDateTime now = LocalDateTime.now();
        AchievementReview review = new AchievementReview()
                .setAchievementId(achievement.getId())
                .setAchievementDocId(achievementDocId)
                .setReviewerId(reviewer.getId())
                .setReviewerName(reviewerName == null ? "" : reviewerName)
                .setAction(reviewRequest.getAction().toLowerCase())
                .setComment(reviewRequest.getComment())
                .setPreviousStatus(previousStatus)
                .setNewStatus(newStatus)
                .setCreatedAt(now)
                .setUpdatedAt(now)
                .setIsDelete(0);
        achievementReviewMapper.insert(review);

        LambdaUpdateWrapper<AchievementReviewerAssignment> assignmentUpdate = new LambdaUpdateWrapper<>();
        assignmentUpdate.eq(AchievementReviewerAssignment::getAchievementId, achievement.getId())
                .eq(AchievementReviewerAssignment::getReviewerId, reviewer.getUuid())
                .eq(AchievementReviewerAssignment::getIsDelete, 0)
                .eq(AchievementReviewerAssignment::getStatus, "pending")
                .set(AchievementReviewerAssignment::getStatus, "completed")
                .set(AchievementReviewerAssignment::getCompletedAt, now)
                .set(AchievementReviewerAssignment::getUpdatedAt, now);
        assignmentMapper.update(null, assignmentUpdate);

        // 构建返回结果
        ReviewResultVO result = new ReviewResultVO();
        result.setAchievementDocId(achievementDocId);
        result.setStatus(newStatus);
        result.setReviewerId(reviewer.getId());
        result.setReviewerName(reviewerName);
        result.setComment(reviewRequest.getComment());
        result.setReviewedAt(now);
        result.setSuccess(true);
        result.setMessage(message);

        log.info("审核完成: achievementDocId={}, newStatus={}", achievementDocId, newStatus);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResultVO assignReviewers(String achievementDocId, AssignReviewerDTO assignReviewerDTO,
            KeycloakUser assigner) {
        log.info("分配审核人: achievementDocId={}, reviewerIds={}, assignerId={}",
                achievementDocId, assignReviewerDTO.getReviewerIds(), assigner.getUuid());

        // 查询成果
        AchievementMains achievement = getAchievementByDocId(achievementDocId);
        if (achievement == null) {
            throw new RuntimeException("成果不存在");
        }

        // 检查是否有审核人
        if (assignReviewerDTO.getReviewerIds() == null || assignReviewerDTO.getReviewerIds().isEmpty()) {
            throw new RuntimeException("审核人列表不能为空");
        }

        // 取第一个审核人作为当前审核人
        Integer firstReviewerId = assignReviewerDTO.getReviewerIds().get(0);
        String firstReviewerName = resolveReviewerName(assignReviewerDTO, firstReviewerId, 0);
        String assignerName = assigner.getName();
        LocalDateTime now = LocalDateTime.now();

        // 更新成果的审核人信息
        LambdaUpdateWrapper<AchievementMains> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AchievementMains::getDocumentId, achievementDocId)
                .set(AchievementMains::getReviewerId, firstReviewerId)
                .set(firstReviewerName != null, AchievementMains::getReviewerName, firstReviewerName)
                .set(AchievementMains::getAchievementStatus, "UNDER_REVIEW")
                .set(AchievementMains::getUpdatedAt, now);

        achievementMainsMapper.update(null, updateWrapper);

        for (int i = 0; i < assignReviewerDTO.getReviewerIds().size(); i++) {
            Integer reviewerId = assignReviewerDTO.getReviewerIds().get(i);
            String reviewerName = resolveReviewerName(assignReviewerDTO, reviewerId, i);
            AchievementReviewerAssignment assignment = new AchievementReviewerAssignment()
                    .setAchievementId(achievement.getId())
                    .setAchievementDocId(achievementDocId)
                    .setReviewerId(reviewerId)
                    .setReviewerName(reviewerName == null ? "" : reviewerName)
                    .setAssignedById(assigner.getId())
                    .setAssignedByName(assignerName)
                    .setStatus("pending")
                    .setAssignedAt(now)
                    .setCreatedAt(now)
                    .setUpdatedAt(now)
                    .setIsDelete(0);
            assignmentMapper.insert(assignment);
        }

        // 构建返回结果
        ReviewResultVO result = new ReviewResultVO();
        result.setAchievementDocId(achievementDocId);
        result.setReviewerId(firstReviewerId);
        result.setReviewerName(firstReviewerName);
        result.setSuccess(true);
        result.setMessage("审核人分配成功");

        log.info("审核人分配完成: achievementDocId={}, reviewerId={}", achievementDocId, firstReviewerId);

        return result;
    }

    /**
     * 根据documentId查询成果
     */
    private AchievementMains getAchievementByDocId(String documentId) {
        LambdaQueryWrapper<AchievementMains> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AchievementMains::getDocumentId, documentId)
                .eq(AchievementMains::getIsDelete, 0)
                .isNotNull(AchievementMains::getPublishedAt);
        return achievementMainsMapper.selectOne(queryWrapper);
    }

    private String resolveReviewerName(AssignReviewerDTO dto, Integer reviewerId, int index) {
        if (dto != null && dto.getReviewerNames() != null && index < dto.getReviewerNames().size()) {
            String name = dto.getReviewerNames().get(index);
            if (name != null && !name.isBlank()) {
                return name.trim();
            }
        }
        return keycloakUserService.getUserById(reviewerId).getName();
    }

    @Override
    public Page<ReviewBacklogVO> pageReviewBacklog(Integer reviewerId, Integer pageNum, Integer pageSize) {
        log.info("查询审核待办: reviewerId={}, page={}, size={}", reviewerId, pageNum, pageSize);

        // 参数校验
        pageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        pageSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        if (pageSize > 100) {
            pageSize = 100;
        }

        // 创建分页对象
        Page<ReviewBacklogVO> page = new Page<>(pageNum, pageSize);

        // 执行查询
        Page<ReviewBacklogVO> result = achievementMainsMapper.pageReviewBacklog(page, reviewerId);

        log.info("查询审核待办完成: total={}, records={}", result.getTotal(), result.getRecords().size());

        return result;
    }

    @Override
    public Page<ReviewHistoryVO> pageReviewHistory(Integer reviewerId, Integer pageNum, Integer pageSize) {
        log.info("查询审核历史: reviewerId={}, page={}, size={}", reviewerId, pageNum, pageSize);

        // 参数校验
        pageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        pageSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        if (pageSize > 100) {
            pageSize = 100;
        }

        // 创建分页对象
        Page<ReviewHistoryVO> page = new Page<>(pageNum, pageSize);

        // 执行查询
        Page<ReviewHistoryVO> result = achievementMainsMapper.pageReviewHistory(page, reviewerId);

        log.info("查询审核历史完成: total={}, records={}", result.getTotal(), result.getRecords().size());

        return result;
    }
}
