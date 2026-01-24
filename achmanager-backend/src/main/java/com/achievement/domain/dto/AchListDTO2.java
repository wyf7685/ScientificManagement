package com.achievement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

@Tag(name="用户查询所有可见成果物列表的查询条件")
@Data
public class AchListDTO2 {
    /** 页码，从 1 开始 */
    @Schema(description = "页码，默认从 1 开始")
    private Integer pageNum = 1;
    @Schema(description = "每页大小，默认10")
    /** 每页大小 */
    private Integer pageSize = 10;
    @Schema(description = "按成果物类型id 搜索")
    /** 成果物类型ID（achievement_types.id） */
    private Integer typeId;
    @Schema(description = "按成果物类型Code搜索")
    private String typeCode;
    @Schema(description = "按成果物最近范围搜索")
    private String recentRange; //一年/三年/五年/全部
    /// TODO 目前缺少用户ID
    private Integer creatorId;
    /** 可见范围（后面你加字段再用） */
    @Schema(description = "按成果物可见范围搜索,不需要传")
    private String visibilityRange;
}
