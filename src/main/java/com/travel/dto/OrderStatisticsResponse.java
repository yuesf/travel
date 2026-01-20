package com.travel.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 订单统计响应DTO
 * 
 * @author travel-platform
 */
@Data
public class OrderStatisticsResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 待支付订单数量
     */
    private Long pendingPay;
    
    /**
     * 待使用订单数量（已支付）
     */
    private Long pendingUse;
    
    /**
     * 已完成订单数量
     */
    private Long completed;
    
    /**
     * 已取消订单数量
     */
    private Long cancelled;
    
    public OrderStatisticsResponse() {
        this.pendingPay = 0L;
        this.pendingUse = 0L;
        this.completed = 0L;
        this.cancelled = 0L;
    }
}
