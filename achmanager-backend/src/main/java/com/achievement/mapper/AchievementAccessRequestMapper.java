package com.achievement.mapper;

import com.achievement.domain.dto.AccessRequestQueryDTO;
import com.achievement.domain.po.AchievementAccessRequest;
import com.achievement.domain.vo.AccessRequestVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * 成果访问权限申请 Mapper 接口
 */
public interface AchievementAccessRequestMapper extends BaseMapper<AchievementAccessRequest> {
    
    /**
     * 分页查询访问申请列表
     */
    Page<AccessRequestVO> pageAccessRequests(Page<?> page, @Param("dto") AccessRequestQueryDTO dto);
}
