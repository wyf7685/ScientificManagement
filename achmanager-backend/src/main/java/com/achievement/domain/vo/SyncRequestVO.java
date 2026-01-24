package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 同步请求VO
 */
@Data
@Schema(description = "同步请求")
public class SyncRequestVO {

    @Schema(description = "项目ID")
    private String projectId;
}