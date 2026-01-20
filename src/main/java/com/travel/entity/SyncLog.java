package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 同步日志实体类
 * 
 * @author travel-platform
 */
@Data
public class SyncLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 同步类型
     */
    private String syncType;
    
    /**
     * 同步配置ID
     */
    private Long syncConfigId;
    
    /**
     * 状态：SUCCESS-成功，FAILED-失败
     */
    private String status;
    
    /**
     * 总记录数
     */
    private Integer totalCount;
    
    /**
     * 成功数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failedCount;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
