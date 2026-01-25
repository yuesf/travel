package com.travel.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件记录查询请求DTO
 * 
 * @author travel-platform
 */
@Data
public class FileRecordQueryRequest {
    
    /**
     * 文件类型（image/video/all）
     */
    private String fileType = "all";
    
    /**
     * 模块名称（common/article/banner等）
     */
    private String module;
    
    /**
     * 存储类型（OSS/LOCAL/all）
     */
    private String storageType = "all";
    
    /**
     * 搜索关键词（文件名）
     */
    private String keyword;
    
    /**
     * 开始日期
     */
    private LocalDateTime startDate;
    
    /**
     * 结束日期
     */
    private LocalDateTime endDate;
    
    /**
     * 页码（从1开始）
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}
