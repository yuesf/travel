package com.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 缓存刷新请求DTO
 * 
 * @author travel-platform
 */
@Data
@Schema(description = "缓存刷新请求")
public class CacheRefreshRequest {

    /**
     * 缓存类型（必填）
     * 支持的值：home, miniprogram, product, article, attraction, payment
     */
    @NotBlank(message = "缓存类型不能为空")
    @Schema(description = "缓存类型", example = "product", 
            allowableValues = {"home", "miniprogram", "product", "article", "attraction", "payment"})
    private String cacheType;

    /**
     * 数据ID列表（可选）
     * 如果提供，只刷新指定ID的数据
     * 如果不提供，刷新该类型的所有数据
     */
    @Schema(description = "数据ID列表（可选，不提供则刷新全部）", example = "[1, 2, 3]")
    private List<Long> ids;

    /**
     * 是否异步执行（可选，默认false）
     * true: 异步执行，立即返回任务ID
     * false: 同步执行，等待完成后返回结果
     */
    @Schema(description = "是否异步执行", example = "false", defaultValue = "false")
    private Boolean async;

    /**
     * 获取是否异步执行（带默认值）
     * 
     * @return 是否异步执行，默认false
     */
    public Boolean getAsync() {
        return async != null ? async : false;
    }
}
