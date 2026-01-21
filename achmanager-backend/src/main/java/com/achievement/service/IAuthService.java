package com.achievement.service;

import com.achievement.domain.dto.LoginDTO;
import com.achievement.domain.vo.LoginVO;

/**
 * 认证服务接口
 *
 * @author system
 * @since 2026-01-21
 */
public interface IAuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求
     * @return 登录响应
     */
    LoginVO login(LoginDTO loginDTO);
}
