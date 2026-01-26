package com.achievement.mapper;

import com.achievement.domain.dto.AchFieldRow;
import com.achievement.domain.dto.AchListDTO;
import com.achievement.domain.dto.AchListDTO2;
import com.achievement.domain.dto.AchMainBaseRow;
import com.achievement.domain.po.AchievementMains;
import com.achievement.domain.vo.*;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author author
 * @since 2025-12-11
 */
public interface AchievementMainsMapper extends BaseMapper<AchievementMains> {

    /**
     * 管理员分页查询成果物列表（带类型名、创建者名等）
     */
    Page<AchListVO> pageList(Page<?> page, @Param("dto") AchListDTO dto);
    // 统计某用户的成果物总数
    Integer countTotalByUser(@Param("userId") Integer userId);
    //统计某用户本月新增成果物数量
    Integer countMonthNewByUser(@Param("userId") Integer userId,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);
    //统计某用户的某类型成果物数量
    Integer countByTypeCodeForUser(@Param("userId") Integer userId,
                                   @Param("typeCode") String typeCode);

    // 统计某用户的成果物总数
    Integer countTotal();
    //统计某用户本月新增成果物数量
    Integer countMonthNew(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);
    //统计某用户的某类型成果物数量
    Integer countByTypeCode(@Param("typeCode") String typeCode);
    /**
     * 根据成果物类型统计数量
     */
    List<TypeCountVO> countByType(@Param("creatorId") Long creatorId);
    
    /**
     * 查成果物主表
     */
    AchMainBaseRow selectMainBaseByDocId(@Param("docId") String docId);
    
    /**
     * 查成果物字段行
     */
    List<AchFieldRow> selectFieldRows(@Param("mainId") Integer mainId,
                                      @Param("typeId") Integer typeId);

    Page<AchListVO> pageList2(Page<?> page, @Param("dto") AchListDTO2 achListDTO);
    
    /**
     * 分页查询审核待办列表
     */
    Page<ReviewBacklogVO> pageReviewBacklog(Page<?> page, @Param("reviewerId") Integer reviewerId);
    
    /**
     * 分页查询审核历史列表
     */
    Page<ReviewHistoryVO> pageReviewHistory(Page<?> page, @Param("reviewerId") Integer reviewerId);

    List<YearTrendItem> countYearlyTrendByUser(@Param("userId") Integer userId, @Param("fromYear") Integer fromYear,
                                               @Param("toYear") Integer toYear);
}
