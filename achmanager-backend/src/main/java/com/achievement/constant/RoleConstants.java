package com.achievement.constant;

/**
 * 角色常量定义
 * 统一定义所有系统角色，避免硬编码字符串
 *
 * @author system
 * @since 2026-01-31
 */
public class RoleConstants {

    /**
     * 科研管理员
     */
    public static final String RESEARCH_ADMIN = "research_admin";

    /**
     * 科研专家/评审专家
     */
    public static final String RESEARCH_EXPERT = "research_expert";

    /**
     * 项目负责人/申报者
     */
    public static final String PROJECT_LEADER = "project_leader";

    /**
     * 决策机构
     */
    public static final String DECISION_ORG = "decision_org";

    // Keycloak 内部角色（需要过滤掉）
    public static final String OFFLINE_ACCESS = "offline_access";
    public static final String UMA_AUTHORIZATION = "uma_authorization";
    public static final String DEFAULT_ROLE = "default-roles-research-management";

    /**
     * 获取所有业务角色
     */
    public static final String[] BUSINESS_ROLES = {
            RESEARCH_ADMIN,
            RESEARCH_EXPERT,
            PROJECT_LEADER,
            DECISION_ORG
    };

    /**
     * 获取所有内部角色（需要过滤）
     */
    public static final String[] INTERNAL_ROLES = {
            OFFLINE_ACCESS,
            UMA_AUTHORIZATION,
            DEFAULT_ROLE
    };

    private RoleConstants() {
        // Prevent instantiation
    }
}
