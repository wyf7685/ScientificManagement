package com.achievement.mapper;

import com.achievement.domain.po.ProcessSubmissionFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 过程系统提交物文件表 Mapper 接口
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
public interface ProcessSubmissionFileMapper extends BaseMapper<ProcessSubmissionFile> {

    /**
     * 根据提交物ID查询文件列表
     */
    List<ProcessSubmissionFile> selectBySubmissionId(@Param("submissionId") Long submissionId);

    /**
     * 根据文件分类查询文件列表
     */
    List<ProcessSubmissionFile> selectBySubmissionIdAndCategory(@Param("submissionId") Long submissionId,
                                                              @Param("fileCategory") String fileCategory);

    /**
     * 根据文件ID查询文件信息
     */
    ProcessSubmissionFile selectByFileId(@Param("fileId") String fileId);

    /**
     * 批量插入文件记录
     */
    int batchInsert(@Param("files") List<ProcessSubmissionFile> files);

    /**
     * 更新文件存储状态
     */
    int updateStorageStatus(@Param("fileId") String fileId, @Param("status") String status);
}