package com.travel.config;

import com.travel.dto.InitAdminRequest;
import com.travel.service.AdminInitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 管理员自动初始化组件
 * 系统启动时自动检查并创建默认超级管理员账号
 * 
 * @author travel-platform
 */
@Slf4j
@Component
@Order(1) // 设置执行顺序，确保在其他组件之前执行
public class AdminInitRunner implements ApplicationRunner {
    
    @Autowired
    private AdminInitService adminInitService;
    
    @Value("${travel.admin.init.enabled:true}")
    private boolean initEnabled;
    
    @Value("${travel.admin.init.default-username:admin}")
    private String defaultUsername;
    
    @Value("${travel.admin.init.default-password:admin123}")
    private String defaultPassword;
    
    @Value("${travel.admin.init.default-real-name:系统管理员}")
    private String defaultRealName;
    
    @Value("${travel.admin.init.default-email:admin@travel.com}")
    private String defaultEmail;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!initEnabled) {
            log.info("管理员自动初始化已禁用");
            return;
        }
        
        try {
            // 检查是否已存在超级管理员
            if (adminInitService.hasSuperAdmin()) {
                log.info("超级管理员已存在，跳过自动初始化");
                return;
            }
            
            // 创建默认超级管理员
            log.info("开始自动初始化超级管理员账号...");
            InitAdminRequest request = new InitAdminRequest();
            request.setUsername(defaultUsername);
            request.setPassword(defaultPassword);
            request.setRealName(defaultRealName);
            request.setEmail(defaultEmail);
            
            adminInitService.initSuperAdmin(request);
            log.info("超级管理员自动初始化成功！用户名: {}, 密码: {}", defaultUsername, defaultPassword);
            log.warn("⚠️  请及时修改默认密码以确保系统安全！");
            
        } catch (Exception e) {
            log.error("自动初始化超级管理员失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }
}
