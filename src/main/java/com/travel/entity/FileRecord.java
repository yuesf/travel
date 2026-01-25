package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件记录实体类
 * 
 * @author travel-platform
 */
@Data
public class FileRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
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
     * 文件访问URL（完整URL）
     */
    private String fileUrl;
    
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
}
