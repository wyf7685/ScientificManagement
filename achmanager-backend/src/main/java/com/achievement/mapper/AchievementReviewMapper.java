package com.achievement.mapper;

import com.achievement.domain.po.AchievementReview;
import com.achievement.domain.vo.ReviewHistoryVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 成果审核记录Mapper
 */
@Mapper
public interface AchievementReviewMapper extends BaseMapper<AchievementReview> {

    @Select("""
        SELECT
            am.document_id AS documentId,
            am.title AS title,
            am.project_name AS projectName,
            am.project_code AS projectCode,
            at.type_name AS typeName,
            ar.action AS reviewResult,
            ar.created_at AS reviewedAt
        FROM achievement_reviews ar
        INNER JOIN achievement_mains am ON ar.achievement_id = am.id
        LEFT JOIN achievement_mains_achievement_type_id_lnk aml ON am.id = aml.achievement_main_id
        LEFT JOIN achievement_types at ON aml.achievement_type_id = at.id
        WHERE ar.is_delete = 0
          AND ar.reviewer_id = #{reviewerId}
        ORDER BY ar.created_at DESC
        """)
    IPage<ReviewHistoryVO> selectReviewHistory(Page<?> page, @Param("reviewerId") Integer reviewerId);
}
