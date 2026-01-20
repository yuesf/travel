package com.travel.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 外部酒店房型数据DTO（用于接收外部API数据）
 * 
 * @author travel-platform
 */
@Data
public class ExternalHotelRoom {
    
    /**
     * 房型名称
     */
    private String roomType;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 床型
     */
    private String bedType;
    
    /**
     * 面积（平方米）
     */
    private BigDecimal area;
    
    /**
     * 设施列表
     */
    private List<String> facilities;
    
    /**
     * 图片列表
     */
    private List<String> images;
}
