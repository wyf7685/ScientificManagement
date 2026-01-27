package com.achievement.service;

import com.achievement.domain.dto.AchListDTO;
import com.achievement.domain.dto.AchListDTO2;
import com.achievement.domain.dto.TrendQueryDTO;
import com.achievement.domain.po.AchievementMains;
import com.achievement.domain.vo.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2025-12-11
 */
public interface IAchievementMainsService extends IService<AchievementMains> {

    Page<AchListVO> pageList(AchListDTO achListDTO);

    UserStatVo countByUserId(Integer userId);

    Long countByUserIdAndTypeId(Long typeId, Long userId);

    Long countMonthNewByUserId(Long userId);

    Long countAch();

    Long countByTypeId(Long typeId);

    Long countMonthNew();

    Page<AchListVO> pageList4User(AchListDTO achListDTO,Integer userId);

    AchDetailVO selectDetail(String achDocId);

    Page<AchListVO> pageList4Visibility(AchListDTO2 achListDTO);

    UserStatVo countstatistics();

    TypeYearTrendVo typeYearTrend(TrendQueryDTO trendQueryDTO);

    Page<AchListVO> pageList4Admin(AchListDTO2 achListDTO);
}
