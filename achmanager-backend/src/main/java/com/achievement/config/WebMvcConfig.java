package com.achievement.config;

import com.achievement.interceptor.ProcessSystemApiLogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC配置
 *
 * @author system
 * @since 2026-01-21
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;
    private final ProcessSystemApiLogInterceptor processSystemApiLogInterceptor;

    /**
     * 添加自定义参数解析器
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加过程系统API日志拦截器
        registry.addInterceptor(processSystemApiLogInterceptor)
                .addPathPatterns("/api/v1/process-system/**")
                .order(1);
    }
}
