package com.achievement.service;

import com.achievement.domain.po.InterimResultView;
import com.achievement.domain.vo.InterimResultListVO;
import com.achievement.domain.vo.InterimResultStatsVO;
import com.achievement.domain.vo.InterimResultVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;

/**
 * <p>
 * 中期成果物视图服务接口
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
public interface IInterimResultViewService extends IService<InterimResultView> {

    /**
     * 分页查询中期成果物
     *
     * @param projectId 项目ID
     * @param type 成果物类型
     * @param projectName 项目名称
     * @param submitter 提交者
     * @param page 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    InterimResultListVO getInterimResults(String projectId, String type, String projectName, 
                                        String submitter, String keyword, String year,
                                        Integer page, Integer pageSize);

    /**
     * 查询中期成果物列表（不分页）
     *
     * @param projectId 项目ID
     * @param type 成果物类型
     * @param projectName 项目名称
     * @param submitter 提交者
     * @param keyword 关键字
     * @param year 年份
     * @return 成果物列表
     */
    java.util.List<InterimResultVO> listInterimResults(String projectId, String type, String projectName,
                                                       String submitter, String keyword, String year);

    /**
     * 获取统计数据
     *
     * @return 统计数据
     */
    InterimResultStatsVO getStats();

    /**
     * 根据ID获取详情
     *
     * @param id 成果物ID
     * @return 成果物详情
     */
    InterimResultView getDetailById(String id);

    /**
     * 根据ID获取详情（VO）
     *
     * @param id 成果物ID
     * @return 成果物详情
     */
    InterimResultVO getDetailVO(String id);

    /**
     * 根据时间范围查询成果物
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 成果物列表
     */
    InterimResultListVO getInterimResultsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
}
