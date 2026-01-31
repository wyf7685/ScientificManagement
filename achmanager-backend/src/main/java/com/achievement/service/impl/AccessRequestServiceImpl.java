package com.achievement.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.achievement.domain.dto.AccessRequestQueryDTO;
import com.achievement.domain.dto.AccessRequestReviewDTO;
import com.achievement.domain.dto.KeycloakUser;
import com.achievement.domain.po.AchievementAccessRequest;
import com.achievement.domain.po.AchievementMains;
import com.achievement.domain.vo.AccessRequestVO;
import com.achievement.mapper.AchievementAccessRequestMapper;
import com.achievement.mapper.AchievementMainsMapper;
import com.achievement.service.IAccessRequestService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 访问权限申请服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccessRequestServiceImpl implements IAccessRequestService {

    private final AchievementAccessRequestMapper accessRequestMapper;
    private final AchievementMainsMapper achievementMainsMapper;

    @Override
    public Page<AccessRequestVO> pageAccessRequests(AccessRequestQueryDTO queryDTO) {
        log.info("查询访问申请列表: keyword={}, status={}", queryDTO.getKeyword(), queryDTO.getStatus());

        // 参数校验
        int pageNum = (queryDTO.getPage() == null || queryDTO.getPage() < 1) ? 1 : queryDTO.getPage();
        int pageSize = (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) ? 10 : queryDTO.getPageSize();
        if (pageSize > 100) {
            pageSize = 100;
        }

        // 创建分页对象
        Page<AccessRequestVO> page = new Page<>(pageNum, pageSize);

        // 执行查询
        Page<AccessRequestVO> result = accessRequestMapper.pageAccessRequests(page, queryDTO);

        log.info("查询访问申请完成: total={}, records={}", result.getTotal(), result.getRecords().size());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccessRequestVO reviewAccessRequest(
            String requestId,
            AccessRequestReviewDTO reviewDTO,
            KeycloakUser reviewer) {

        log.info("审核访问申请: requestId={}, action={}, reviewerId={}",
                requestId, reviewDTO.getAction(), reviewer.getId());

        // 1. 查询申请记录
        LambdaQueryWrapper<AchievementAccessRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AchievementAccessRequest::getDocumentId, requestId)
                .eq(AchievementAccessRequest::getIsDelete, 0);
        AchievementAccessRequest request = accessRequestMapper.selectOne(queryWrapper);

        if (request == null) {
            throw new RuntimeException("访问申请不存在");
        }

        // 2. 检查状态
        if (!"pending".equals(request.getStatus())) {
            throw new RuntimeException("该申请已被审核，无法重复审核");
        }

        String action = reviewDTO.getAction() == null ? "" : reviewDTO.getAction().trim().toLowerCase();
        if (!"approve".equals(action) && !"reject".equals(action)) {
            throw new RuntimeException("无效的审核动作: " + reviewDTO.getAction());
        }
        // 3. 确定新状态
        String newStatus = "approve".equals(action) ? "approved" : "rejected";

        String reviewerName = reviewer.getName();

        // 4. 更新申请状态
        LambdaUpdateWrapper<AchievementAccessRequest> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AchievementAccessRequest::getDocumentId, requestId)
                .set(AchievementAccessRequest::getStatus, newStatus)
                .set(AchievementAccessRequest::getReviewerId, reviewer.getId())
                .set(AchievementAccessRequest::getReviewerName, reviewerName)
                .set(AchievementAccessRequest::getReviewComment, reviewDTO.getComment())
                .set(AchievementAccessRequest::getReviewedAt, LocalDateTime.now())
                .set(AchievementAccessRequest::getUpdatedAt, LocalDateTime.now());

        accessRequestMapper.update(null, updateWrapper);

        if ("approved".equals(newStatus)) {
            updateVisibilityOnApprove(request.getAchievementDocId());
        }

        // 5. 构建返回结果
        AccessRequestVO result = new AccessRequestVO();
        result.setId(requestId);
        result.setResultId(request.getAchievementDocId());
        result.setResultTitle(request.getAchievementTitle());
        result.setUserId(request.getRequesterId());
        result.setUserName(request.getRequesterName());
        result.setReason(request.getReason());
        result.setStatus(newStatus);
        result.setReviewer(reviewerName);
        result.setComment(reviewDTO.getComment());
        result.setCreatedAt(request.getCreatedAt());
        result.setReviewedAt(LocalDateTime.now());

        log.info("访问申请审核完成: requestId={}, newStatus={}", requestId, newStatus);

        return result;
    }

    private void updateVisibilityOnApprove(String achievementDocId) {
        if (achievementDocId == null || achievementDocId.isBlank()) {
            return;
        }
        LambdaQueryWrapper<AchievementMains> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AchievementMains::getDocumentId, achievementDocId)
                .eq(AchievementMains::getIsDelete, 0);
        AchievementMains achievement = achievementMainsMapper.selectOne(queryWrapper);
        if (achievement == null) {
            return;
        }
        String visibility = achievement.getVisibilityRange();
        String updatedVisibility = switch (visibility) {
            case "internal_abstract" -> "internal_full";
            case "public_abstract" -> "public_full";
            default -> visibility;
        };
        if (updatedVisibility != null && !updatedVisibility.equals(visibility)) {
            LambdaUpdateWrapper<AchievementMains> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AchievementMains::getDocumentId, achievementDocId)
                    .set(AchievementMains::getVisibilityRange, updatedVisibility)
                    .set(AchievementMains::getUpdatedAt, LocalDateTime.now());
            achievementMainsMapper.update(null, updateWrapper);
        }
    }
}
