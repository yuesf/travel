package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小程序配置实体类
 * 
 * @author travel-platform
 */
@Data
public class MiniProgramConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 配置键（唯一）
     */
    private String configKey;
    
    /**
     * 配置值（JSON格式）
     */
    private String configValue;
    
    /**
     * 配置类型：BANNER-轮播图，ICON-图标，RECOMMEND-推荐，CATEGORY-分类，AD-广告位
     */
    private String configType;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
