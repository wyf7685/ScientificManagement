package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Schema(description = "用户统计信息返回对象")
public class UserStatVo {
    private Integer totalResults ;//用户成果物哦总数
    private Integer monthlyNew ;//用户本月新增成果物数
    private Integer paperCount ;//用户成果物类型数
    private Integer patentCount;
}
