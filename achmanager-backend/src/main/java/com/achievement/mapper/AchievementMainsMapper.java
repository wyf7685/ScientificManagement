package com.achievement.mapper;

import com.achievement.domain.dto.AchFieldRow;
import com.achievement.domain.dto.AchListDTO;
import com.achievement.domain.dto.AchListDTO2;
import com.achievement.domain.dto.AchMainBaseRow;
import com.achievement.domain.po.AchievementMains;
import com.achievement.domain.vo.AchListVO;
import com.achievement.domain.vo.TypeCountVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author author
 * @since 2025-12-11
 */
public interface AchievementMainsMapper extends BaseMapper<AchievementMains> {

    /**
     * 管理员分页查询成果物列表（带类型名、创建者名等）
     */
    Page<AchListVO> pageList(Page<?> page, @Param("dto") AchListDTO dto);
    //根据成果物类型统计数量
    List<TypeCountVO> countByType(@Param("creatorId") Long creatorId);
    //查成果物 主表
    AchMainBaseRow selectMainBaseByDocId(@Param("docId") String docId);
    //查成果物 字段行
    List<AchFieldRow> selectFieldRows(@Param("mainId") Integer mainId,
                                      @Param("typeId") Integer typeId);

    Page<AchListVO> pageList2(Page<?> page,@Param("dto") AchListDTO2 achListDTO);
}
