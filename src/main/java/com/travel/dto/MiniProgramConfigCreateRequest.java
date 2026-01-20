package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建小程序配置请求DTO
 * 
 * @author travel-platform
 */
@Data
public class MiniProgramConfigCreateRequest {
    
    /**
     * 配置键（唯一）
     */
    @NotBlank(message = "配置键不能为空")
    private String configKey;
    
    /**
     * 配置值（JSON格式）
     */
    private String configValue;
    
    /**
     * 配置类型：BANNER-轮播图，RECOMMEND-推荐，CATEGORY-分类，AD-广告位
     */
    @NotBlank(message = "配置类型不能为空")
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
