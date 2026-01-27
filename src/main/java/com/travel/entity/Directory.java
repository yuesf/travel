package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件目录实体类
 * 
 * @author travel-platform
 */
@Data
public class Directory implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 目录名称
     */
    private String name;
    
    /**
     * 目录路径（如 "common/subfolder"）
     */
    private String path;
    
    /**
     * 父目录ID（null表示根目录）
     */
    private Long parentId;
    
    /**
     * 目录层级（1-根目录，2-子目录，3-三级目录等）
     */
    private Integer level;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 创建人ID
     */
    private Long createdBy;
    
    /**
     * 子目录列表（用于树形结构）
     */
    private List<Directory> children;
}
