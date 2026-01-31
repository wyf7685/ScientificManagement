package com.achievement.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import com.achievement.config.KeycloakConfig;
import com.achievement.domain.dto.KeycloakUser;
import com.achievement.domain.po.BusinessUser;
import com.achievement.mapper.BusinessUserMapper;
import com.achievement.service.IKeycloakUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Keycloak 用户服务实现
 * 通过 Keycloak Admin API 查询和管理用户
 *
 * @author system
 * @since 2026-01-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserServiceImpl implements IKeycloakUserService {

    private final Keycloak keycloak;
    private final KeycloakConfig keycloakConfig;
    private final BusinessUserMapper businessUserMapper;

    @Override
    public Integer getOrCreateUserId(String keycloakUserId) {
        LambdaQueryWrapper<BusinessUser> query = new LambdaQueryWrapper<>();
        query.eq(BusinessUser::getKeycloakUserId, keycloakUserId);
        BusinessUser user = businessUserMapper.selectOne(query);
        if (user != null) {
            return user.getId();
        } else {
            BusinessUser newUser = new BusinessUser();
            newUser.setKeycloakUserId(keycloakUserId);
            businessUserMapper.insert(newUser);
            return newUser.getId();
        }
    }

    private RealmResource getRealmResource() {
        return keycloak.realm(keycloakConfig.getRealm());
    }

    @Override
    public KeycloakUser getUserById(String keycloakUserId) {
        try {
            UserRepresentation userRep = getRealmResource()
                    .users().get(keycloakUserId).toRepresentation();

            return convertToKeycloakUser(userRep);
        } catch (Exception e) {
            log.error("Failed to get user by id: {}", keycloakUserId, e);
            return null;
        }
    }

    @Override
    public KeycloakUser getUserById(Integer userId) {
        LambdaQueryWrapper<BusinessUser> query = new LambdaQueryWrapper<>();
        query.eq(BusinessUser::getId, userId);
        BusinessUser user = businessUserMapper.selectOne(query);
        if (user == null) {
            return null;
        }
        return getUserById(user.getKeycloakUserId());
    }

    @Override
    public List<KeycloakUser> getUsersByRole(String role) {
        try {
            return getRealmResource().roles().get(role).getUserMembers().stream()
                    .map(this::convertToKeycloakUser)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get users by role: {}", role, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<KeycloakUser> searchUsers(String query) {
        try {
            return getRealmResource().users().search(query).stream()
                    .map(this::convertToKeycloakUser)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to search users with query: {}", query, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<KeycloakUser> getAllUsers() {
        try {
            return getRealmResource().users().list().stream()
                    .map(this::convertToKeycloakUser)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get all users", e);
            return Collections.emptyList();
        }
    }

    /**
     * 将 Keycloak UserRepresentation 转换为 KeycloakUser DTO
     *
     * @param userRep Keycloak 用户表示
     * @return KeycloakUser DTO
     */
    private KeycloakUser convertToKeycloakUser(UserRepresentation userRep) {
        if (userRep == null) {
            return null;
        }

        return KeycloakUser.builder()
                .id(getOrCreateUserId(userRep.getId()))
                .uuid(userRep.getId())
                .username(userRep.getUsername())
                .email(userRep.getEmail())
                .name(buildFullName(userRep.getFirstName(), userRep.getLastName()))
                .roles(userRep.getRealmRoles())
                .enabled(userRep.isEnabled())
                .build();
    }

    /**
     * 构建完整姓名
     *
     * @param firstName 名字
     * @param lastName  姓氏
     * @return 完整姓名
     */
    private String buildFullName(String firstName, String lastName) {
        List<String> nameParts = new ArrayList<>();

        if (firstName != null && !firstName.isEmpty()) {
            nameParts.add(firstName);
        }

        if (lastName != null && !lastName.isEmpty()) {
            nameParts.add(lastName);
        }

        return String.join(" ", nameParts);
    }
}
