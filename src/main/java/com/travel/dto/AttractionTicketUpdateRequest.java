package com.travel.dto;

import lombok.Data;

import java.math.BigDecimal;
/**
 * 更新具体票种请求DTO
 * 
 * @author travel-platform
 */
@Data
public class AttractionTicketUpdateRequest {
    
    /**
     * 票种名称
     */
    private String name;
    
    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
