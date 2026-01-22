package com.achievement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

/**
 * 管理员查询成果物列表的查询条件
 */
@Data
@Tag(name = "查询成果物列表的查询条件")
public class AchListDTO {

    /** 页码，从 1 开始 */
    @Schema(description = "页码，默认从 1 开始")
    private Integer pageNum = 1;
    @Schema(description = "每页大小，默认10")
    /** 每页大小 */
    private Integer pageSize = 10;
    @Schema(description = "按成果物关键词模糊搜索")
    /** 按成果物关键词模糊搜索 */
    private String mainTitle;
    @Schema(description = "按成果物摘类型搜索")
    /** 成果物类型ID（achievement_types.id） */
    private Integer typeId;

    @Schema(description = "按成果物类型名称搜索")
    private String typeCode;
    @Schema(description = "按成果物审核状态搜索")
    /** 审核状态（achievement_status） */
    private String status;
    @Schema(description = "按成果物所属项目Id")
    /** 项目名（后面你加了项目表再用） */
    private Integer projectId;
    /// TODO 目前缺少用户ID
    private Integer creatorId;
    /** 可见范围（后面你加字段再用） */
    private String visibilityRange;
}
