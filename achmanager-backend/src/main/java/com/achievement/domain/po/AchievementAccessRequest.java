package com.achievement.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 成果访问权限申请表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("achievement_access_requests")
@Schema(name = "AchievementAccessRequest对象", description = "成果访问权限申请")
public class AchievementAccessRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String documentId;

    private Integer achievementId;

    private String achievementDocId;

    private String achievementTitle;

    private Integer requesterId;

    private String requesterName;

    private String reason;

    private String status;

    private Integer reviewerId;

    private String reviewerName;

    private String reviewComment;

    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    private Integer createdById;

    private Integer updatedById;

    private String locale;

    private Integer isDelete;
}
