package com.achievement.service.impl;

import com.achievement.domain.vo.InterimResultVO;
import com.achievement.service.IExcelExportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Excel导出服务实现
 */
@Slf4j
@Service
public class ExcelExportServiceImpl implements IExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ByteArrayOutputStream exportInterimResultsToExcel(List<InterimResultVO> interimResults) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportInterimResultsToExcel(interimResults, outputStream);
        return outputStream;
    }

    @Override
    public void exportInterimResultsToExcel(List<InterimResultVO> interimResults, ByteArrayOutputStream outputStream) throws IOException {
        log.info("开始导出中期成果物Excel，数据量: {}", interimResults.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("中期成果物列表");

            // 创建标题样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "成果物ID", "项目ID", "项目名称", "项目编码", "项目阶段",
                    "成果物名称", "成果物类型", "成果物描述", "提交者", "提交者部门",
                    "提交时间", "同步时间", "数据来源", "来源引用ID", "状态",
                    "标签", "附件数量", "附件列表"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据行
            int rowIndex = 1;
            for (InterimResultVO result : interimResults) {
                Row dataRow = sheet.createRow(rowIndex++);

                // 成果物ID
                createCell(dataRow, 0, result.getId(), dataStyle);
                // 项目ID
                createCell(dataRow, 1, result.getProjectId(), dataStyle);
                // 项目名称
                createCell(dataRow, 2, result.getProjectName(), dataStyle);
                // 项目编码
                createCell(dataRow, 3, result.getProjectCode(), dataStyle);
                // 项目阶段
                createCell(dataRow, 4, result.getProjectPhase(), dataStyle);
                // 成果物名称
                createCell(dataRow, 5, result.getName(), dataStyle);
                // 成果物类型
                createCell(dataRow, 6, result.getTypeLabel(), dataStyle);
                // 成果物描述
                createCell(dataRow, 7, result.getDescription(), dataStyle);
                // 提交者
                createCell(dataRow, 8, result.getSubmitter(), dataStyle);
                // 提交者部门
                createCell(dataRow, 9, result.getSubmitterDept(), dataStyle);
                // 提交时间
                createCell(dataRow, 10, 
                        result.getSubmittedAt() != null ? result.getSubmittedAt().format(DATE_FORMATTER) : "", 
                        dataStyle);
                // 同步时间
                createCell(dataRow, 11, 
                        result.getSyncedAt() != null ? result.getSyncedAt().format(DATE_FORMATTER) : "", 
                        dataStyle);
                // 数据来源
                createCell(dataRow, 12, result.getSource(), dataStyle);
                // 来源引用ID
                createCell(dataRow, 13, result.getSourceRef(), dataStyle);
                // 状态
                createCell(dataRow, 14, result.getStatus(), dataStyle);
                // 标签
                createCell(dataRow, 15, 
                        result.getTags() != null ? String.join(", ", result.getTags()) : "", 
                        dataStyle);
                // 附件数量
                createCell(dataRow, 16, 
                        result.getAttachments() != null ? String.valueOf(result.getAttachments().size()) : "0", 
                        dataStyle);
                // 附件列表
                createCell(dataRow, 17, buildAttachmentList(result.getAttachments()), dataStyle);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 设置最大列宽，避免过宽
                int columnWidth = sheet.getColumnWidth(i);
                if (columnWidth > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }

            // 写入输出流
            workbook.write(outputStream);
            
            log.info("中期成果物Excel导出完成，数据量: {}", interimResults.size());
        }
    }

    /**
     * 创建标题样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        
        // 设置对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置字体
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        
        // 设置对齐
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 设置自动换行
        style.setWrapText(true);
        
        return style;
    }

    /**
     * 创建单元格
     */
    private void createCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(StringUtils.hasText(value) ? value : "");
        cell.setCellStyle(style);
    }

    /**
     * 构建附件列表字符串
     */
    private String buildAttachmentList(List<InterimResultVO.AttachmentVO> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < attachments.size(); i++) {
            InterimResultVO.AttachmentVO attachment = attachments.get(i);
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(attachment.getName());
            if (attachment.getSize() != null) {
                sb.append(" (").append(formatFileSize(attachment.getSize())).append(")");
            }
        }
        
        return sb.toString();
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long size) {
        if (size == null || size <= 0) {
            return "0B";
        }
        
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double fileSize = size.doubleValue();
        
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f%s", fileSize, units[unitIndex]);
    }
}