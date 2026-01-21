package com.achievement.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author author
 * @since 2025-12-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("achievement_mains")
@Schema(name="AchievementMains对象", description="")
public class AchievementMains implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String documentId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    private Integer createdById;

    private Integer updatedById;

    private String locale;

    private String title;

    private String achievementStatus;

    private String summary;

    private Integer isDelete;

    private String visibilityRange;

    private Integer creatorId;

    private String creatorName;

    private String creatorDept;

    private Integer reviewerId;

    private String reviewerName;

    private String reviewComment;

    private LocalDateTime reviewedAt;


}
