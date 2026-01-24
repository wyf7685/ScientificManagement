package com.achievement.mapper;

import com.achievement.domain.po.ProcessSubmissionFile;
import org.apache.ibatis.annotations.Param;

/**
 * 中期成果物文件查询 Mapper
 */
public interface InterimResultFileMapper {

    /**
     * 根据文件ID查询文件元数据
     *
     * @param fileId 文件ID
     * @return 文件元数据
     */
    ProcessSubmissionFile selectFileById(@Param("fileId") String fileId);
}
