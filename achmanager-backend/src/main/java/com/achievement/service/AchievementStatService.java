package com.achievement.service;

import com.achievement.domain.vo.TypeCountVO;

import java.util.List;

public interface AchievementStatService {
    List<TypeCountVO> typePie(Integer creatorId);
}
