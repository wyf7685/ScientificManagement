package com.achievement.service;

import com.achievement.domain.vo.InterimResultVO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Excel导出服务接口
 */
public interface IExcelExportService {

    /**
     * 导出中期成果物列表为Excel
     *
     * @param interimResults 中期成果物列表
     * @return Excel文件字节数组
     * @throws IOException IO异常
     */
    ByteArrayOutputStream exportInterimResultsToExcel(List<InterimResultVO> interimResults) throws IOException;

    /**
     * 导出中期成果物列表为Excel并写入输出流
     *
     * @param interimResults 中期成果物列表
     * @param outputStream 输出流
     * @throws IOException IO异常
     */
    void exportInterimResultsToExcel(List<InterimResultVO> interimResults, ByteArrayOutputStream outputStream) throws IOException;
}