package com.travel.dto;

import lombok.Data;

/**
 * 文件统计响应DTO
 * 
 * @author travel-platform
 */
@Data
public class FileStatisticsResponse {
    
    /**
     * 总文件数
     */
    private Integer totalCount;
    
    /**
     * 总文件大小（字节）
     */
    private Long totalSize;
    
    /**
     * OSS文件数
     */
    private Integer ossCount;
    
    /**
     * OSS文件大小（字节）
     */
    private Long ossSize;
    
    /**
     * 本地文件数
     */
    private Integer localCount;
    
    /**
     * 本地文件大小（字节）
     */
    private Long localSize;
    
    /**
     * 总文件大小（格式化后）
     */
    private String totalSizeFormatted;
    
    /**
     * OSS文件大小（格式化后）
     */
    private String ossSizeFormatted;
    
    /**
     * 本地文件大小（格式化后）
     */
    private String localSizeFormatted;
}
