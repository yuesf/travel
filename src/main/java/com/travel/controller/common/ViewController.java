package com.travel.controller.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 前端视图控制器
 * 处理 Vue Router History 模式的路由回退
 * 只处理前端路由请求，返回 index.html
 * 静态资源请求由 Spring Boot 的静态资源处理器处理，不会到达这里
 * 
 * @author travel-platform
 */
@Slf4j
@Controller
public class ViewController {
    
    /**
     * 处理前端路由请求，返回 index.html
     * 使用精确的路径匹配，避免拦截静态资源请求
     * 静态资源请求（/assets/**、*.js、*.css 等）由 FileUploadConfig 配置的静态资源处理器处理
     */
    @RequestMapping(value = {"/", "/login", "/dashboard", "/attractions/**", "/hotels/**", 
                              "/products/**", "/categories/**", "/articles/**", "/miniprogram/**", 
                              "/system/**", "/403", "/404"})
    public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getRequestURI();
        log.debug("ViewController 处理请求: {}", path);
        
        // 返回 index.html，让 Vue Router 处理路由
        log.info("返回 index.html 给路径: {}", path);
        try {
            Resource resource = new ClassPathResource("static/index.html");
            if (resource.exists()) {
                log.debug("找到 index.html，准备返回");
                response.setContentType(MediaType.TEXT_HTML_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                resource.getInputStream().transferTo(response.getOutputStream());
                log.debug("成功返回 index.html");
            } else {
                log.warn("index.html 未找到，请确保前端已正确构建到 static 目录");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IOException e) {
            log.error("读取 index.html 失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
