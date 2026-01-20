package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 订单创建请求DTO
 * 
 * @author travel-platform
 */
@Data
public class OrderCreateRequest {
    
    /**
     * 订单类型：ATTRACTION-景点，HOTEL-酒店，PRODUCT-商品
     */
    @NotBlank(message = "订单类型不能为空")
    private String orderType;
    
    /**
     * 购物车ID列表（从购物车结算时使用）
     */
    private List<Long> cartIds;
    
    /**
     * 订单项列表（直接购买时使用）
     */
    private List<OrderItemRequest> items;
    
    /**
     * 使用的优惠券ID（可选）
     */
    private Long couponId;
    
    /**
     * 联系人姓名
     */
    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;
    
    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;
    
    /**
     * 备注（可选）
     */
    private String remark;
    
    /**
     * 订单项请求DTO
     */
    @Data
    public static class OrderItemRequest {
        /**
         * 明细类型：ATTRACTION-景点，HOTEL_ROOM-酒店房型，PRODUCT-商品
         */
        @NotBlank(message = "明细类型不能为空")
        private String itemType;
        
        /**
         * 明细ID（景点ID/房型ID/商品ID）
         */
        @NotNull(message = "明细ID不能为空")
        private Long itemId;
        
        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        private Integer quantity;
        
        /**
         * 入住日期（酒店，可选）
         */
        private LocalDate checkInDate;
        
        /**
         * 退房日期（酒店，可选）
         */
        private LocalDate checkOutDate;
        
        /**
         * 使用日期（景点，可选）
         */
        private LocalDate useDate;
    }
}
