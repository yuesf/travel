package com.travel.controller.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 前端视图控制器
 * 
 * 注意：前端静态资源已分离到 nginx，不再由后端提供
 * 此控制器已废弃，保留仅用于向后兼容（如果需要）
 * 
 * @author travel-platform
 * @deprecated 前端已分离到 nginx，不再需要此控制器
 */
@Slf4j
@Controller
@Deprecated
public class ViewController {
    
    /**
     * 处理前端路由请求，返回 index.html
     * 
     * @deprecated 前端已分离到 nginx，不再需要此方法
     */
    @Deprecated
    @RequestMapping(value = {"/", "/login", "/dashboard", "/attractions/**", "/hotels/**", 
                              "/products/**", "/categories/**", "/articles/**", "/miniprogram/**", 
                              "/system/**", "/403", "/404",
                              "/travel", "/travel/", "/travel/login", "/travel/dashboard", "/travel/attractions/**", 
                              "/travel/hotels/**", "/travel/products/**", "/travel/categories/**", 
                              "/travel/articles/**", "/travel/miniprogram/**", "/travel/system/**", 
                              "/travel/403", "/travel/404"})
    public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getRequestURI();
        log.warn("ViewController 收到请求: {}，但前端已分离到 nginx，此请求不应到达后端", path);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}
