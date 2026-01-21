package com.achievement.service;

import com.achievement.domain.dto.AccessRequestQueryDTO;
import com.achievement.domain.dto.AccessRequestReviewDTO;
import com.achievement.domain.vo.AccessRequestVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 访问权限申请服务接口
 */
public interface IAccessRequestService {
    
    /**
     * 分页查询访问申请列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<AccessRequestVO> pageAccessRequests(AccessRequestQueryDTO queryDTO);
    
    /**
     * 审核访问申请
     * @param requestId 申请ID
     * @param reviewDTO 审核信息
     * @param reviewerId 审核人ID
     * @return 审核结果
     */
    AccessRequestVO reviewAccessRequest(String requestId, AccessRequestReviewDTO reviewDTO, Integer reviewerId);
}
