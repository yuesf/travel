package com.travel.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件记录响应DTO
 * 注意：previewUrl字段是签名URL（OSS文件），可直接使用；fileUrl是原始URL，用于存储
 * 
 * @author travel-platform
 */
@Data
public class FileRecordResponse {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 文件名（UUID生成）
     */
    private String fileName;
    
    /**
     * 原始文件名
     */
    private String originalName;
    
    /**
     * 文件路径（相对路径）
     */
    private String filePath;
    
    /**
     * 文件访问URL（完整URL，原始URL，用于存储）
     */
    private String fileUrl;
    
    /**
     * 预览URL（签名URL，用于预览，仅OSS文件有值）
     */
    private String previewUrl;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型（image/video）
     */
    private String fileType;
    
    /**
     * 文件扩展名（jpg/png/mp4等）
     */
    private String fileExtension;
    
    /**
     * 模块名称（common/article/banner等）
     */
    private String module;
    
    /**
     * 存储类型（OSS/LOCAL）
     */
    private String storageType;
    
    /**
     * 上传时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 上传人ID
     */
    private Long createdBy;
    
    /**
     * 文件大小（格式化后，如：1.5 MB）
     */
    private String fileSizeFormatted;
}
