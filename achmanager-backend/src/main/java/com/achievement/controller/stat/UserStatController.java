package com.achievement.controller.stat;

import com.achievement.result.Result;
import com.achievement.service.IAchievementMainsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

//12.14 统计某用户的成果物数量
@Slf4j
@RestController
@RequestMapping("/user/stat/")
@RequiredArgsConstructor
@Tag(name="统计用户的成果物")
public class UserStatController {
    private final IAchievementMainsService mainsService;

    //统计某用户的成果物数量
    @Operation(description = "统计某用户的成果物数量")
    @GetMapping("/userAchCount")
    public Result<Long> userAchStat(){

        Long count = mainsService.countByUserId();
        return Result.success(count);

    }
    //统计某用户的某类别成果物数量
    @Operation(description = "统计某用户创建的某类别成果物数量")
    @GetMapping("/userTypeAchCount")
    public Result<Long> userTypeAchStat(@RequestParam Long typeId){

        Long count = mainsService.countByUserIdAndTypeId(typeId);
        return Result.success(count);

    }
    @Operation(description = "统计某用户的本月新增成果物数量")
    //统计某用户的本月新增成果物数量
    @GetMapping("/userMonthNewAchCount")
    public Result<Long> userMonthNewAchStat(){
        Long count = mainsService.countMonthNewByUserId();
        return Result.success(count);
    }


}
