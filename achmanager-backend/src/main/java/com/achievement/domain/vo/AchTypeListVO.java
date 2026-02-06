package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "成果类型列表返回VO")
public class AchTypeListVO {
    //成果类型ID
    @Schema(description = "成果类型ID")
    private Integer id;

    @Schema(description = "成果类型UUID")
    private String documentId;
    //成果类型编码英文名
    @Schema(description = "成果类型编码英文名")
    private String typeCode;
    //成果类型名称中文名
    @Schema(description = "成果类型名称中文名")
    private String typeName;
    //成果类型描述
    @Schema(description = "成果类型描述")
    private String description;
    @Schema(description = "是否启用")
    private Integer enabled;

}
