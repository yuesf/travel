package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建票种分类请求DTO
 * 
 * @author travel-platform
 */
@Data
public class AttractionTicketCategoryCreateRequest {
    
    /**
     * 景点ID（从路径参数获取，不需要验证）
     */
    private Long attractionId;
    
    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;
    
    /**
     * 分类描述
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
