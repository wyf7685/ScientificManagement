package com.achievement.mapper;

import com.achievement.domain.po.InterimResultView;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 中期成果物视图 Mapper 接口
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
public interface InterimResultViewMapper extends BaseMapper<InterimResultView> {

    /**
     * 分页查询中期成果物
     */
    Page<InterimResultView> selectPageByConditions(Page<?> page,
                                                  @Param("projectId") Long projectId,
                                                  @Param("type") String type,
                                                  @Param("projectName") String projectName,
                                                  @Param("submitter") String submitter,
                                                  @Param("keyword") String keyword,
                                                  @Param("year") Integer year,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 查询中期成果物列表（不分页）
     */
    List<InterimResultView> selectByConditions(@Param("projectId") Long projectId,
                                               @Param("type") String type,
                                               @Param("projectName") String projectName,
                                               @Param("submitter") String submitter,
                                               @Param("keyword") String keyword,
                                               @Param("year") Integer year,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计项目总数
     */
    @Select("SELECT COUNT(DISTINCT project_id) FROM interim_results_view")
    Integer countTotalProjects();

    /**
     * 统计成果物总数
     */
    @Select("SELECT COUNT(*) FROM interim_results_view")
    Integer countTotalResults();

    /**
     * 按类型统计
     */
    @Select("SELECT type, COUNT(*) as count FROM interim_results_view GROUP BY type")
    List<Map<String, Object>> countByType();

    /**
     * 按年份统计
     */
    @Select("SELECT upload_year, COUNT(*) as count FROM interim_results_view GROUP BY upload_year ORDER BY upload_year")
    List<Map<String, Object>> countByYear();

    /**
     * 获取最近同步时间
     */
    @Select("SELECT MAX(synced_at) FROM interim_results_view")
    LocalDateTime getRecentSyncTime();

    /**
     * 根据项目ID查询成果物
     */
    List<InterimResultView> selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据类型查询成果物
     */
    List<InterimResultView> selectByType(@Param("type") String type);

    /**
     * 根据提交者查询成果物
     */
    List<InterimResultView> selectBySubmitter(@Param("submitter") String submitter);

    /**
     * 根据时间范围查询成果物
     */
    List<InterimResultView> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);
}
