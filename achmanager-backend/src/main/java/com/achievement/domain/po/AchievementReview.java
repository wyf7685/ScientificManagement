package com.achievement.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 成果审核记录
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("achievement_reviews")
public class AchievementReview implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String documentId;

    private Integer achievementId;

    private String achievementDocId;

    private Integer reviewerId;

    private String reviewerName;

    private String action;

    private String comment;

    private String previousStatus;

    private String newStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    private Integer createdById;

    private Integer updatedById;

    private String locale;

    private Integer isDelete;
}
