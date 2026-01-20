package com.travel.dto;

import lombok.Data;

/**
 * 同步日志列表查询请求DTO
 * 
 * @author travel-platform
 */
@Data
public class SyncLogListRequest {
    
    /**
     * 同步类型
     */
    private String syncType;
    
    /**
     * 状态：SUCCESS-成功，FAILED-失败
     */
    private String status;
    
    /**
     * 页码（从1开始）
     */
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}
