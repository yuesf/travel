package com.travel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加购物车请求DTO
 * 
 * @author travel-platform
 */
@Data
public class CartAddRequest {
    
    /**
     * 商品类型：ATTRACTION-景点，HOTEL_ROOM-酒店房型，PRODUCT-商品
     */
    @NotBlank(message = "商品类型不能为空")
    private String itemType;
    
    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long itemId;
    
    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;
}
