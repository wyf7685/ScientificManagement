package com.achievement.config;

import com.achievement.annotation.CurrentUser;
import com.achievement.domain.po.BusinessUser;
import com.achievement.mapper.BusinessUserMapper;
import com.achievement.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 当前用户参数解析器
 * 从JWT令牌中解析用户信息并注入到Controller方法参数
 *
 * @author system
 * @since 2026-01-21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtUtil jwtUtil;
    private final BusinessUserMapper businessUserMapper;

    /**
     * 判断是否支持该参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && BusinessUser.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * 解析参数
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                   ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest,
                                   WebDataBinderFactory binderFactory) {
        
        // 从请求头获取token
        String authorization = webRequest.getHeader("Authorization");
        
        if (!StringUtils.hasText(authorization)) {
            log.warn("请求头中未找到Authorization");
            throw new RuntimeException("未登录，请先登录");
        }

        // 移除 "Bearer " 前缀
        String token = authorization.startsWith("Bearer ") 
                ? authorization.substring(7) 
                : authorization;

        try {
            // 解析token获取用户ID
            Integer userId = jwtUtil.getUserIdFromToken(token);
            
            // 从数据库查询用户信息
            BusinessUser user = businessUserMapper.selectById(userId);
            
            if (user == null) {
                log.warn("用户不存在: userId={}", userId);
                throw new RuntimeException("用户不存在");
            }

            if (!Boolean.TRUE.equals(user.getIsActive())) {
                log.warn("用户已被禁用: userId={}", userId);
                throw new RuntimeException("用户已被禁用");
            }

            if (user.getIsDelete() != null && user.getIsDelete() != 0) {
                log.warn("用户已被删除: userId={}", userId);
                throw new RuntimeException("用户不存在");
            }

            return user;
            
        } catch (RuntimeException e) {
            log.error("解析用户信息失败: {}", e.getMessage());
            throw e;
        }
    }
}
