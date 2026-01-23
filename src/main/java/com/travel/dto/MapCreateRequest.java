package com.travel.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建地图请求DTO
 * 
 * @author travel-platform
 */
@Data
public class MapCreateRequest {
    
    /**
     * 地图名称
     */
    @NotBlank(message = "地图名称不能为空")
    private String name;
    
    /**
     * 经度
     */
    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180", message = "经度范围：-180 到 180")
    @DecimalMax(value = "180", message = "经度范围：-180 到 180")
    private BigDecimal longitude;
    
    /**
     * 纬度
     */
    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90", message = "纬度范围：-90 到 90")
    @DecimalMax(value = "90", message = "纬度范围：-90 到 90")
    private BigDecimal latitude;
    
    /**
     * 地址描述
     */
    private String address;
    
    /**
     * 公告内容
     */
    private String announcement;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @Min(value = 0, message = "状态值：0-禁用，1-启用")
    @Max(value = 1, message = "状态值：0-禁用，1-启用")
    private Integer status;
}
