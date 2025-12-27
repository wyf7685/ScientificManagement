package com.achievement.service.impl;

import com.achievement.client.StrapiClient;
import com.achievement.domain.po.AchievementFieldDefs;
import com.achievement.domain.po.AchievementFieldDefsAchievementTypeIdLnk;
import com.achievement.domain.po.AchievementTypes;
import com.achievement.domain.vo.AchTypeDef;
import com.achievement.domain.vo.AchTypeDetailVO;
import com.achievement.domain.vo.AchTypeListVO;
import com.achievement.mapper.AchievementFieldDefsAchievementTypeIdLnkMapper;
import com.achievement.mapper.AchievementFieldDefsMapper;
import com.achievement.mapper.AchievementTypesMapper;
import com.achievement.service.IAchievementTypesService;
import com.achievement.utils.TwoLevelCache;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.achievement.constant.CacheKey.ACHIEVEMENT_TYPE_LIST_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2025-12-11
 */
@Service
@RequiredArgsConstructor
public class AchievementTypesServiceImpl extends ServiceImpl<AchievementTypesMapper, AchievementTypes> implements IAchievementTypesService {
    private final TwoLevelCache twoLevelCache;
    private final AchievementFieldDefsMapper fieldDefsMapper;
    private final AchievementFieldDefsAchievementTypeIdLnkMapper fieldDefsTypeLnkMapper;
    private final StrapiClient strapiClient;
    private final ObjectMapper objectMapper; // ✅ 注入统一的 ObjectMapper

    //缓存key
    @Override
    @SuppressWarnings("unchecked")
    public List<AchTypeListVO> listAchType() {
        //成果物类型 一般不会怎么修改，规定好缓存TTL 为30分钟
        long ttlSeconds = 30 * 60;
        //调用二级缓存工具。
        List<AchTypeListVO> list = twoLevelCache.get(ACHIEVEMENT_TYPE_LIST_KEY,
                List.class,
                //dbLoader,当两级缓存都没有命中的情况，执行一下的逻辑。
                () -> {
                    //到数据库中进行查询，并调用缓存工具
                    List<AchievementTypes> typesList = this.lambdaQuery()
                            .eq(AchievementTypes::getIsDelete, 0)
                            .orderByAsc(AchievementTypes::getId)
                            .isNotNull(AchievementTypes::getPublishedAt) // ✅ 只查询已发布
                            .list();
                    // 通过数据流的方式应用toVo的转换函数，将实体类转换成vo
                    //map操作是对流中的每个函数应用一个函数，将结果作为新的元素放入到流中，
                    //this::toVo 是方法引用，等同于应用了本ServiceImpl下的toVo方法，collect收集结果
                    //Collectors.toList() 将流转换为列表鹅旅行
                    return typesList.stream().map(this::toVo).collect(Collectors.toList());
                },
                ttlSeconds
                );
         return list;
    }



    // 内部方法，用于把数据库的实体类映射到 返回类型VO上
    private AchTypeListVO toVo(AchievementTypes type){
        AchTypeListVO vo = new AchTypeListVO();
        vo.setId(type.getId());
        vo.setDocumentId(type.getDocumentId());
        vo.setTypeCode(type.getTypeCode());
        vo.setTypeName(type.getTypeName());
        vo.setDescription(type.getDescription());
        return vo;
    }
    /**
     * 后续在新增/修改/删除类型时，数据保存到数据库之后记得调用这个方法清缓存!!!
     */
    private void evictTypeListCache() {
        twoLevelCache.evict(ACHIEVEMENT_TYPE_LIST_KEY);
    }

    /**
     * 单个成果物类型详情：类型基本信息+字段定义列表
     * 按 document_id 查询类型详情（只返回已发布版本）
     */
    @Override
    public AchTypeDetailVO selectDetail(String typeDocId) {
        AchievementTypes type = this.lambdaQuery()
                .eq(AchievementTypes::getIsDelete, 0)
                .eq(AchievementTypes::getDocumentId, typeDocId)
                .isNotNull(AchievementTypes::getPublishedAt) // ✅ 只要已发布
                .one();

        if (type == null) {
            throw new RuntimeException("成果物类型不存在或未发布");
        }

        return buildDetailVO(type);
    }
    private AchTypeDetailVO buildDetailVO(AchievementTypes type) {
        Integer typeId = type.getId();

        AchTypeDetailVO vo = new AchTypeDetailVO();
        vo.setId(type.getId());
        vo.setDocumentId(type.getDocumentId());
        vo.setTypeCode(type.getTypeCode());
        vo.setTypeName(type.getTypeName());
        vo.setDescription(type.getDescription());

        // 1) 查 link：类型 -> 字段定义（按排序）
        List<AchievementFieldDefsAchievementTypeIdLnk> links = fieldDefsTypeLnkMapper.selectList(
                new LambdaQueryWrapper<AchievementFieldDefsAchievementTypeIdLnk>()
                        .eq(AchievementFieldDefsAchievementTypeIdLnk::getAchievementTypeId, typeId)
                        .orderByAsc(AchievementFieldDefsAchievementTypeIdLnk::getAchievementFieldDefOrd)
        );

        if (links.isEmpty()) {
            vo.setFieldDefinitions(Collections.emptyList());
            return vo;
        }

        // 2) 提取字段定义 ids
        List<Integer> fieldDefIds = links.stream()
                .map(AchievementFieldDefsAchievementTypeIdLnk::getAchievementFieldDefId)
                .toList();

        // 3) 一次性查字段定义
        List<AchievementFieldDefs> fieldDefs = fieldDefsMapper.selectBatchIds(fieldDefIds);
        if (fieldDefs.isEmpty()) {
            vo.setFieldDefinitions(Collections.emptyList());
            return vo;
        }

        // 4) id -> def 映射，按 link 顺序输出
        Map<Integer, AchievementFieldDefs> defMap = fieldDefs.stream()
                .collect(Collectors.toMap(AchievementFieldDefs::getId, d -> d));

        List<AchTypeDef> fieldDefVos = new ArrayList<>();
        for (AchievementFieldDefsAchievementTypeIdLnk link : links) {
            AchievementFieldDefs def = defMap.get(link.getAchievementFieldDefId());
            if (def == null || Objects.equals(def.getIsDelete(), 1)) {
                continue;
            }
            fieldDefVos.add(toFieldDefVo(def));
        }

        vo.setFieldDefinitions(fieldDefVos);
        return vo;
    }
    private AchTypeDef toFieldDefVo(AchievementFieldDefs def) {
        AchTypeDef vo = new AchTypeDef();
        vo.setId(def.getId());
        vo.setDocumentId(def.getDocumentId());
        vo.setFieldCode(def.getFieldCode());
        vo.setFieldName(def.getFieldName());
        vo.setFieldType(def.getFieldType());
        vo.setDescription(def.getDescription());
        return vo;
    }
    public JsonNode createType(Map<String, Object> req) {
        String raw =  strapiClient.create("achievement-types", req);
        try {
            return new ObjectMapper().readTree(raw);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public JsonNode updateType(String typeDocId, Map<String, Object> req) {

        String raw = strapiClient.update("achievement-types", typeDocId, req);
        //清楚缓存，重新加载
        evictTypeListCache();

        try {
            return objectMapper.readTree(raw);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonNode deleteType(String typeDocId) {
        // 1) 逻辑删除 patch
        Map<String, Object> patch = new HashMap<>();
        patch.put("is_delete", 1);

        // 2) 调 Strapi 更新
        String raw = strapiClient.update("achievement-types", typeDocId, patch);

        // 3) 删除成功后清缓存（非常重要）
        evictTypeListCache();

        // 4) 返回 Strapi 原始结果
        try {
            return objectMapper.readTree(raw);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析 Strapi 响应失败", e);
        }
    }

}
