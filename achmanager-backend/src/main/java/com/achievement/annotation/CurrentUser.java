package com.achievement.annotation;

import java.lang.annotation.*;

/**
 * 当前登录用户注解
 * 用于Controller方法参数，自动注入当前登录的用户对象
 *
 * @author system
 * @since 2026-01-21
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
