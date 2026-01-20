package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建酒店房型请求DTO
 * 
 * @author travel-platform
 */
@Data
public class HotelRoomCreateRequest {
    
    /**
     * 酒店ID
     */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    /**
     * 房型名称
     */
    @NotBlank(message = "房型名称不能为空")
    private String roomType;
    
    /**
     * 价格
     */
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    
    /**
     * 库存
     */
    @NotNull(message = "库存不能为空")
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
    
    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;
}
