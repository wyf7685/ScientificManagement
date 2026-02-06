package com.achievement.mapper;

import com.achievement.domain.po.AchievementReviewerAssignment;
import com.achievement.domain.vo.ReviewAssignVO;
import com.achievement.domain.vo.ReviewBacklogVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 审核人分配Mapper
 */
@Mapper
public interface AchievementReviewerAssignmentMapper extends BaseMapper<AchievementReviewerAssignment> {

    @Select("""
        SELECT
            am.document_id AS documentId,
            am.title AS title,
            am.creator_name AS creatorName,
            am.created_at AS createdAt,
            am.project_name AS projectName,
            am.project_code AS projectCode,
            at.type_name AS typeName,
            ara.assigned_at AS assignedAt
        FROM achievement_reviewer_assignments ara
        INNER JOIN achievement_mains am ON ara.achievement_id = am.id
        LEFT JOIN achievement_mains_achievement_type_id_lnk aml ON am.id = aml.achievement_main_id
        LEFT JOIN achievement_types at ON aml.achievement_type_id = at.id
        WHERE ara.is_delete = 0
          AND ara.status = 'pending'
          AND am.is_delete = 0
          AND am.published_at IS NOT NULL
          AND ara.reviewer_id = #{reviewerId}
        ORDER BY ara.assigned_at DESC
        """)
    IPage<ReviewBacklogVO> selectReviewBacklog(Page<?> page, @Param("reviewerId") Integer reviewerId);

    Page<ReviewAssignVO> pageAssignReviewersList(
            Page<ReviewAssignVO> page
    );

}
