package com.achievement.service;

import java.util.List;

import com.achievement.domain.dto.KeycloakUser;

public interface IKeycloakUserService {

    KeycloakUser getUserById(String keycloakUserId);

    KeycloakUser getUserById(Integer userId);

    List<KeycloakUser> getUsersByRole(String role);

    List<KeycloakUser> searchUsers(String query);

    List<KeycloakUser> getAllUsers();

    Integer getOrCreateUserId(String keycloakUserId);

}
