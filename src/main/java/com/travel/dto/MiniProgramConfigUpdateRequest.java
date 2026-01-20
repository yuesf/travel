package com.travel.dto;

import lombok.Data;

/**
 * 更新小程序配置请求DTO
 * 
 * @author travel-platform
 */
@Data
public class MiniProgramConfigUpdateRequest {
    
    /**
     * 配置值（JSON格式）
     */
    private String configValue;
    
    /**
     * 配置类型：BANNER-轮播图，RECOMMEND-推荐，CATEGORY-分类，AD-广告位
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
}
