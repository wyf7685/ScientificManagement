package com.achievement.service.impl;

import com.achievement.domain.po.AchievementMains;
import com.achievement.domain.vo.TypeCountVO;
import com.achievement.mapper.AchievementMainsMapper;
import com.achievement.service.AchievementStatService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementStatServiceImpl implements AchievementStatService {
    private final AchievementMainsMapper mainsMapper;
    @Override
    public List<TypeCountVO> typePie(Integer creatorId) {
        return mainsMapper.countByType(creatorId);
    }
}
