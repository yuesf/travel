package com.travel.controller.common;

import com.travel.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页控制器
 * 
 * @author travel-platform
 */
@RestController
@Tag(name = "首页")
public class IndexController {
    
    /**
     * 系统信息 API 接口
     * 根路径 "/" 由 ViewController 处理，返回前端应用
     * 系统信息通过 "/api/info" 访问
     */
    @GetMapping(value = "/api/info", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "系统信息 API")
    public Result<Map<String, Object>> info() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "旅游平台系统");
        data.put("version", "1.0.0");
        data.put("description", "旅游平台后台管理系统");
        data.put("swagger", "/swagger-ui/index.html");
        data.put("apiDocs", "/v3/api-docs");
        return Result.success(data);
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public Result<Map<String, String>> health() {
        Map<String, String> data = new HashMap<>();
        data.put("status", "UP");
        data.put("message", "服务运行正常");
        return Result.success(data);
    }
}
