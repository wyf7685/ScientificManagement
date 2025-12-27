package com.achievement.service;

import com.achievement.domain.po.AchievementTypes;
import com.achievement.domain.vo.AchTypeDetailVO;
import com.achievement.domain.vo.AchTypeListVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2025-12-11
 */
public interface IAchievementTypesService extends IService<AchievementTypes> {
    List<AchTypeListVO> listAchType();


    AchTypeDetailVO selectDetail(String typeDocId);

    JsonNode createType(Map<String, Object> req);

    JsonNode updateType(String typeDocId, Map<String, Object> req);

    JsonNode deleteType(String typeDocId);
}
