package com.travel.controller.common;

import com.travel.common.Result;
import com.travel.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 静态资源控制器
 * 处理小程序误请求的静态资源路径，返回 404
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
public class StaticResourceController {
    
    /**
     * 处理 /static/** 路径请求
     * 小程序不应该请求后端的静态资源，这些资源应该在小程序本地
     * 返回 404 错误，提示资源不存在
     */
    @RequestMapping("/static/**")
    public Result<?> handleStaticResource() {
        log.warn("小程序请求了不存在的静态资源路径 /static/**，应该使用本地相对路径");
        return Result.error(ResultCode.NOT_FOUND.getCode(), "静态资源不存在，请使用小程序本地相对路径");
    }
}
