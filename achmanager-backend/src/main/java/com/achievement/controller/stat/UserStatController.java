package com.achievement.controller.stat;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.achievement.annotation.CurrentUser;
import com.achievement.domain.dto.KeycloakUser;
import com.achievement.domain.vo.TypeCountVO;
import com.achievement.domain.vo.UserStatVo;
import com.achievement.result.Result;
import com.achievement.service.AchievementStatService;
import com.achievement.service.IAchievementMainsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//12.14 统计某用户的成果物数量
@Slf4j
@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "统计用户的成果物")
public class UserStatController {
    private final IAchievementMainsService mainsService;
    private final AchievementStatService statService;

    //统计某用户的成果物数量
    @Operation(description = "统计某用户的数据,包含该用户审核通过的成果物数量、论文数、专利数、本月新增")
    @GetMapping("/results/my-statistics")
    public Result<UserStatVo> userAchStat(@CurrentUser KeycloakUser currentUser) {
        UserStatVo statVo = mainsService.countByUserId(currentUser.getId());
        return Result.success(statVo);
    }

    /*
    //统计某用户的某类别成果物数量
    @Operation(description = "统计某用户创建的某类别成果物数量")
    @GetMapping("/userTypeAchCount")
    public Result<Long> userTypeAchStat(@RequestParam Long typeId, @CurrentUser KeycloakUser currentUser){
        Long count = mainsService.countByUserIdAndTypeId(typeId, currentUser.getId());
        return Result.success(count);
    }
    
    @Operation(description = "统计某用户的本月新增成果物数量")
    //统计某用户的本月新增成果物数量
    @GetMapping("/userMonthNewAchCount")
    public Result<Long> userMonthNewAchStat(@CurrentUser KeycloakUser currentUser){
        Long count = mainsService.countMonthNewByUserId(currentUser.getId());
        return Result.success(count);
    }
    */

    @Operation(description = "饼图：各成果物类型数量")
    @GetMapping("/user/stat/typePie")
    public Result<List<TypeCountVO>> typePie(@CurrentUser KeycloakUser currentUser) {
        return Result.success(statService.typePie(Long.valueOf(currentUser.getId())));
    }
}
