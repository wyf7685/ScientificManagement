package com.achievement.controller;

import com.achievement.domain.vo.AchTypeDetailVO;
import com.achievement.domain.vo.AchTypeListVO;
import com.achievement.result.Result;
import com.achievement.service.IAchievementTypesService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/achievementType")
@RequiredArgsConstructor
@Tag(name = "成果物类别相关接口")
//12.11 目前开发成果物类别相关的查询功能
public class AchievementTypeController {
    private final IAchievementTypesService typesService;
    //查询所有成果物类别，只包含启用的类型
    @Operation(description = "查询所有成果物类别")
    @PostMapping("/list")
    public Result<List<AchTypeListVO>> typeList(){
        return Result.success(typesService.listAchType());
    }
    //查询所有成果物类别，包含未启用的类型

    //查询成果物类型的详细属性
    @Operation(description = "查询成果物的详细属性")
    @GetMapping("/detail")
    public Result<AchTypeDetailVO> typeDetail(@RequestParam String typeDocId){
        return Result.success(typesService.selectDetail(typeDocId));
    }

    /*
     *TODO管理员新增成果物类型
     * */
    @PostMapping("/types")
    @Operation(description = "新增成果物类型")
    public JsonNode  createType(@RequestBody Map<String, Object> req) {
        log.info("新增成果物");
        // req 就是前端 JSON 反序列化后的 Map
        return typesService.createType(req);
    }
    /*
     *  管理员更新成果物类型
     * */
    @PutMapping("/types/{typeDocId}")
    @Operation(description = "更新成果物类型")
    public JsonNode updateType(@PathVariable String typeDocId, @RequestBody Map<String, Object> req) {
        log.info("更新成果物类型, typeDocId={}", typeDocId);
        return typesService.updateType(typeDocId, req);
    }
    /**
     * * TODO 管理员删除成果物类型
     */
    @PutMapping("/types/{typeDocId}/delete")
    @Operation(description = "删除成果物类型")
    public JsonNode deleteType(@PathVariable String typeDocId) {
        log.info("删除成果物类型, typeDocId={}", typeDocId);
        return typesService.deleteType(typeDocId);
    }
    /**
     * *管理员 修改成果物启用状态
     */
    @PutMapping("/types/{typeDocId}/enable")
    @Operation(description = "切换成果物类型启用状态")
    public JsonNode toggleEnabledType(@PathVariable String typeDocId) {
        log.info("成果物类型, typeDocId={}", typeDocId);
        return typesService.toggleEnabledType(typeDocId);
    }

    /**
     * 更新成果物类型字段排序
     */
    @PutMapping("/types/{typeDocId}/field-order")
    @Operation(description = "更新成果物类型字段排序")
    public Result<Void> updateFieldOrder(@PathVariable String typeDocId, @RequestBody List<String> fieldDefDocIds) {
        log.info("更新字段排序, typeDocId={}, size={}", typeDocId, fieldDefDocIds == null ? 0 : fieldDefDocIds.size());
        typesService.updateFieldOrder(typeDocId, fieldDefDocIds);
        return Result.success();
    }

}
