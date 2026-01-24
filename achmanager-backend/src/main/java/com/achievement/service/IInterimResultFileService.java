package com.achievement.service;

import com.achievement.domain.po.ProcessSubmissionFile;
import org.springframework.core.io.Resource;

/**
 * 中期成果物文件服务接口
 */
public interface IInterimResultFileService {

    /**
     * 根据文件ID获取文件元数据
     *
     * @param fileId 文件ID
     * @return 文件元数据
     */
    ProcessSubmissionFile getFileMetadata(String fileId);

    /**
     * 根据文件URL获取文件资源
     *
     * @param fileUrl 文件URL
     * @return 文件资源
     */
    Resource getFileResource(String fileUrl);
}
