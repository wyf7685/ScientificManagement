package com.achievement.service.impl;

import com.achievement.annotation.CurrentUser;
import com.achievement.client.StrapiClient;
import com.achievement.domain.dto.AchFieldRow;
import com.achievement.domain.dto.AchListDTO;
import com.achievement.domain.dto.AchListDTO2;
import com.achievement.domain.dto.AchMainBaseRow;
import com.achievement.domain.po.AchievementMains;
import com.achievement.domain.po.AchievementTypes;
import com.achievement.domain.po.BusinessUser;
import com.achievement.domain.vo.AchDetailVO;
import com.achievement.domain.vo.AchFieldVO;
import com.achievement.domain.vo.AchListVO;
import com.achievement.domain.vo.UserStatVo;
import com.achievement.mapper.AchievementMainsMapper;
import com.achievement.service.IAchievementMainsService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.achievement.constant.AchievementStatusConstant.APPROVED;

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
public class AchievementMainsServiceImpl extends ServiceImpl<AchievementMainsMapper, AchievementMains> implements IAchievementMainsService {
    private static final String ACHIEVEMENT_FILE_COLLECTION = "achievement-files";

    private final StrapiClient strapiClient;
    private final ObjectMapper objectMapper;
    private final AchievementMainsMapper mainsMapper;
    @Override
    public Page<AchListVO> pageList(AchListDTO achListDTO){// 兜底：防止 null / 非法值（即使没有校验或校验被改了，其实也安全）
        int pageNum  = (achListDTO.getPageNum()  == null || achListDTO.getPageNum()  < 1)  ? 1  : achListDTO.getPageNum();
        int pageSize = (achListDTO.getPageSize() == null || achListDTO.getPageSize() < 1) ? 10 : achListDTO.getPageSize();
        if (pageSize > 100) {
            pageSize = 100; // 防止一次性查太多
        }
        //MybatisPlus的分页查询
        Page<AchListVO> page = new Page<>(pageNum, pageSize);
        return baseMapper.pageList(page, achListDTO);
    }
    @Override
    public Page<AchListVO> pageList4User(AchListDTO achListDTO,Integer userId){// 兜底：防止 null / 非法值（即使没有校验或校验被改了，其实也安全）
        int pageNum  = (achListDTO.getPageNum()  == null || achListDTO.getPageNum()  < 1)  ? 1  : achListDTO.getPageNum();
        int pageSize = (achListDTO.getPageSize() == null || achListDTO.getPageSize() < 1) ? 10 : achListDTO.getPageSize();
        if (pageSize > 100) {
            pageSize = 100; // 防止一次性查太多
        }
        achListDTO.setCreatorId(userId);
        //MybatisPlus的分页查询
        Page<AchListVO> page = new Page<>(pageNum, pageSize);
        return baseMapper.pageList(page, achListDTO);
    }

    @Override
    public AchDetailVO selectDetail(String achDocId) {
        AchMainBaseRow base = baseMapper.selectMainBaseByDocId(achDocId);
        if (base == null) {
            throw new RuntimeException("成果物不存在或已删除");
        }

        List<AchFieldRow> rows = Collections.emptyList();
        if (base.getMainId() != null && base.getTypeId() != null) {
            rows = baseMapper.selectFieldRows(base.getMainId(), base.getTypeId());
        }

        List<AchFieldVO> fields = rows.stream().map(r -> {
            AchFieldVO vo = new AchFieldVO();
            vo.setId(r.getFieldId());
            vo.setDocumentId(r.getDocumentId());
            vo.setFieldCode(r.getFieldCode());
            vo.setFieldName(r.getFieldName());
            vo.setFieldType(r.getFieldType());
            vo.setIsRequired(r.getIsRequired());

            vo.setValue(pickValueByType(r));
            return vo;
        }).toList();

        AchDetailVO vo = new AchDetailVO();
        vo.setDocumentId(base.getDocumentId());
        vo.setTitle(base.getTitle());
        vo.setSummary(base.getSummary());
        vo.setAuditStatus(base.getAuditStatus());
        vo.setCreatorName(base.getCreatorName());
        vo.setCreatedAt(base.getCreatedAt());
        vo.setUpdatedAt(base.getUpdatedAt());
        vo.setPublishedAt(base.getPublishedAt());

        vo.setTypeDocId(base.getTypeDocId());
        vo.setTypeName(base.getTypeName());
        vo.setTypeCode(base.getTypeCode());
        
        // 设置基础字段
        vo.setYear(base.getYear());
        vo.setProjectCode(base.getProjectCode());
        vo.setProjectName(base.getProjectName());
        vo.setVisibilityRange(base.getVisibilityRange());
        
        // 解析 JSON 数组字段
        vo.setAuthors(parseJsonArray(base.getAuthorsJson()));
        vo.setKeywords(parseJsonArray(base.getKeywordsJson()));

        vo.setFields(fields);

        // 附件信息：通过 Strapi 查询 achievement_files，并 populate media(files)
        try {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("filters[achievement_main_id][documentId][$eq]", achDocId);
            params.put("filters[is_delete][$eq]", "0");
            params.put("populate", "files");
            String raw = strapiClient.query(ACHIEVEMENT_FILE_COLLECTION, params);
            vo.setAttachments(objectMapper.readTree(raw));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return vo;
    }
    
    private List<String> parseJsonArray(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Page<AchListVO> pageList4Visibility(AchListDTO2 achListDTO) {
            int pageNum  = (achListDTO.getPageNum()  == null || achListDTO.getPageNum()  < 1)  ? 1  : achListDTO.getPageNum();
            int pageSize = (achListDTO.getPageSize() == null || achListDTO.getPageSize() < 1) ? 10 : achListDTO.getPageSize();
            if (pageSize > 100) {
                pageSize = 100; // 防止一次性查太多
            }
            //MybatisPlus的分页查询
            Page<AchListVO> page = new Page<>(pageNum, pageSize);
            return baseMapper.pageList2(page, achListDTO);
    }

    @Override
    public UserStatVo countstatistics() {
        // 本月时间范围 [本月第一天00:00:00, 下月第一天00:00:00)
        LocalDate now = LocalDate.now();
        LocalDateTime start = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        Integer total = mainsMapper.countTotal();
        Integer monthNew = mainsMapper.countMonthNew( start, end);
        Integer paperCount = mainsMapper.countByTypeCode( "paper");
        Integer patentCount = mainsMapper.countByTypeCode( "patent");

        UserStatVo vo = new UserStatVo();
        vo.setTotalResults(total == null ? 0 : total);
        vo.setMonthlyNew(monthNew == null ? 0 : monthNew);
        vo.setPaperCount(paperCount == null ? 0 : paperCount);
        vo.setPatentCount(patentCount == null ? 0 : patentCount);
        return vo;
    }

    private Object pickValueByType(AchFieldRow r) {
        if (r.getFieldType() == null) return r.getTextValue();
        return switch (r.getFieldType()) {
            case "text" -> r.getTextValue();
            case "boolean" -> r.getBooleanValue();
            case "number" -> r.getNumberValue();
            case "date" -> r.getDateValue();
            case "email" -> r.getEmailValue();
            default -> r.getTextValue();
        };
    }



    @Override
    public UserStatVo countByUserId(Integer userId) {
        userId = 1;
        if (userId == null) {
            throw new RuntimeException("userId不能为空");
        }

        // 本月时间范围 [本月第一天00:00:00, 下月第一天00:00:00)
        LocalDate now = LocalDate.now();
        LocalDateTime start = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        Integer total = mainsMapper.countTotalByUser(userId);
        Integer monthNew = mainsMapper.countMonthNewByUser(userId, start, end);
        Integer paperCount = mainsMapper.countByTypeCodeForUser(userId, "paper");
        Integer patentCount = mainsMapper.countByTypeCodeForUser(userId, "patent");

        UserStatVo vo = new UserStatVo();
        vo.setTotalResults(total == null ? 0 : total);
        vo.setMonthlyNew(monthNew == null ? 0 : monthNew);
        vo.setPaperCount(paperCount == null ? 0 : paperCount);
        vo.setPatentCount(patentCount == null ? 0 : patentCount);
        return vo;
    }

    @Override
    public Long countByUserIdAndTypeId(Long typeId) {
        Long userId = 1L; //TODO 获取当前用户ID
        return this.lambdaQuery()
                //TODO 数据表尚未修改 .eq(AchievementMains::getCreatorId, userId)
                .eq(AchievementMains::getIsDelete, 0)
                .isNotNull(AchievementMains::getPublishedAt) // ✅ 只要已发布
                .inSql(AchievementMains::getId,
                        "SELECT achievement_main_id " +
                                "FROM achievement_mains_achievement_type_id_lnk " +
                                "WHERE achievement_type_id = " + typeId
                )
                .count();
    }
    @Override
    public Long countMonthNewByUserId() {
        Long userId = 1L;//TODO 获取当前用户ID
        LocalDate now = LocalDate.now();
        LocalDateTime firstDay = now.withDayOfMonth(1).atStartOfDay(); // 本月1号 00:00:00
        LocalDateTime firstDayNextMonth = firstDay.plusMonths(1); // 下月1号 00:00:00
        Long count = this.lambdaQuery()
                //TODO 数据表尚未修改 .eq(AchievementMains::getCreatorId, userId)
                .eq(AchievementMains::getIsDelete, 0)
                .eq(AchievementMains::getAchievementStatus,APPROVED) //具体可修改，此处规定为审核通过的成果物
                .isNotNull(AchievementMains::getPublishedAt) // ✅ 只要已发布
                .ge(AchievementMains::getCreatedAt, firstDay)        // >= 本月1号 00:00:00
                .lt(AchievementMains::getCreatedAt, firstDayNextMonth) // < 下月1号 00:00:00

                .count();
        return count;
    }

    @Override
    public Long countByTypeId(Long typeId) {

        return this.lambdaQuery()
                .eq(AchievementMains::getIsDelete, 0)
                .isNotNull(AchievementMains::getPublishedAt) // ✅ 只要已发布
                .inSql(AchievementMains::getId,
                        "SELECT achievement_main_id " +
                                "FROM achievement_mains_achievement_type_id_lnk " +
                                "WHERE achievement_type_id = " + typeId
                )
                .count();
    }

    @Override
    public Long countAch() {
        return this.lambdaQuery()
                .isNotNull(AchievementMains::getPublishedAt) // ✅ 只要已发布;
                .eq(AchievementMains::getIsDelete, 0)
                .count();
    }

    @Override
    public Long countMonthNew() {
        LocalDate now = LocalDate.now();
        LocalDateTime firstDay = now.withDayOfMonth(1).atStartOfDay(); // 本月1号 00:00:00
        LocalDateTime firstDayNextMonth = firstDay.plusMonths(1); // 下月1号 00:00:00
        Long count = this.lambdaQuery()
                .eq(AchievementMains::getIsDelete, 0)
                .eq(AchievementMains::getAchievementStatus,APPROVED) //具体可修改，此处规定为审核通过的成果物
                .isNotNull(AchievementMains::getPublishedAt) // ✅ 只要已发布
                .ge(AchievementMains::getCreatedAt, firstDay)        // >= 本月1号 00:00:00
                .lt(AchievementMains::getCreatedAt, firstDayNextMonth) // < 下月1号 00:00:00
                .count();
        return count;
    }
}
