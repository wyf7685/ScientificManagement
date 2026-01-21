package com.achievement.service.impl;

import com.achievement.domain.dto.LoginDTO;
import com.achievement.domain.po.BusinessUser;
import com.achievement.domain.vo.LoginVO;
import com.achievement.domain.vo.UserProfileVO;
import com.achievement.mapper.BusinessUserMapper;
import com.achievement.service.IAuthService;
import com.achievement.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现
 *
 * @author system
 * @since 2026-01-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final BusinessUserMapper businessUserMapper;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(LoginDTO loginDTO) {
        log.info("用户登录: username={}", loginDTO.getUsername());

        // 查询用户
        LambdaQueryWrapper<BusinessUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BusinessUser::getUsername, loginDTO.getUsername())
                .eq(BusinessUser::getIsDelete, 0);

        BusinessUser user = businessUserMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("用户已被禁用");
        }

        // 简化版：暂不验证密码（后续可添加密码字段和加密逻辑）
        // 实际生产环境应该验证密码哈希
        // if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
        //     throw new RuntimeException("密码错误");
        // }

        // 更新最后登录时间
        LambdaUpdateWrapper<BusinessUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(BusinessUser::getId, user.getId())
                .set(BusinessUser::getLastLoginAt, LocalDateTime.now());
        businessUserMapper.update(null, updateWrapper);

        // 生成JWT令牌
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                loginDTO.getRemember()
        );

        UserProfileVO profile = UserProfileVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .phone(user.getPhone())
                .build();

        // 构建返回结果
        LoginVO loginVO = LoginVO.builder()
                .token(token)
                .user(profile)
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .phone(user.getPhone())
                .build();

        log.info("登录成功: userId={}, role={}", user.getId(), user.getRole());

        return loginVO;
    }
}
