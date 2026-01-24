package com.achievement.config;

import com.achievement.filter.ProcessSystemApiKeyFilter;
import com.achievement.service.ProcessSystemRateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 过程系统安全配置
 *
 * @author system
 * @since 2026-01-23
 */
@Configuration
@RequiredArgsConstructor
public class ProcessSystemSecurityConfig {

    private final ProcessSystemProperties processSystemProperties;
    private final ProcessSystemRateLimitService rateLimitService;

    /**
     * 注册过程系统API密钥验证过滤器
     */
    @Bean
    public FilterRegistrationBean<ProcessSystemApiKeyFilter> processSystemApiKeyFilter() {
        FilterRegistrationBean<ProcessSystemApiKeyFilter> registrationBean = new FilterRegistrationBean<>();
        
        ProcessSystemApiKeyFilter filter = new ProcessSystemApiKeyFilter(processSystemProperties, rateLimitService);
        registrationBean.setFilter(filter);
        
        // 只对过程系统API路径生效
        registrationBean.addUrlPatterns("/api/v1/process-system/*");
        
        // 设置过滤器优先级
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        
        registrationBean.setName("processSystemApiKeyFilter");
        
        return registrationBean;
    }
}