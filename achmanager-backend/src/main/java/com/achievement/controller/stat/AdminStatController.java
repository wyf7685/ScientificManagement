package com.achievement.controller.stat;

import com.achievement.domain.vo.TypeCountVO;
import com.achievement.result.Result;
import com.achievement.service.AchievementStatService;
import com.achievement.service.IAchievementMainsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/stat/")
@Tag(name = "管理员统计成果物")
@RequiredArgsConstructor
public class AdminStatController {

    private final IAchievementMainsService mainsService;
    private final AchievementStatService statService;
        //统计成果物数量
        @Operation(description = "统计所有用户的成果物数量")
        @GetMapping("/AchCount")
        public Result<Long> AchStat(){
            Long count = mainsService.countAch();
            return Result.success(count);

        }
        //统计某类别成果物数量
        @Operation(description = "统计创建的某类别成果物数量")
        @GetMapping("/TypeAchCount")
        public Result<Long> TypeAchStat(@RequestParam Long typeId){

            Long count = mainsService.countByTypeId(typeId);
            return Result.success(count);

        }
        @Operation(description = "统计系统本月新增成果物数量")
        //统计本月新增成果物数量
        @GetMapping("/MonthNewAchCount")
        public Result<Long> MonthNewAchStat(){
            Long count = mainsService.countMonthNew();
            return Result.success(count);
        }

        /**
            * 饼图：各成果物类型数量
            * creatorId 不传 = 全量（管理员口径）
            * creatorId 传值 = 指定用户
        */
        @Operation(description = "饼图：各成果物类型数量")
        @GetMapping("/typePie")
        public Result<List<TypeCountVO>> typePie() {
        return Result.success(statService.typePie(null));
        }
}
