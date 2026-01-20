package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.InitAdminRequest;
import com.travel.entity.AdminUser;
import com.travel.service.AdminInitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 管理员初始化控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/init")
@Tag(name = "管理员初始化")
public class InitController {
    
    @Autowired
    private AdminInitService adminInitService;
    
    /**
     * 检查是否已初始化
     */
    @GetMapping("/check")
    @Operation(summary = "检查是否已初始化")
    public Result<Boolean> checkInit() {
        boolean hasSuperAdmin = adminInitService.hasSuperAdmin();
        return Result.success(hasSuperAdmin);
    }
    
    /**
     * 初始化超级管理员
     */
    @PostMapping("/super-admin")
    @Operation(summary = "初始化超级管理员")
    public Result<?> initSuperAdmin(@Valid @RequestBody InitAdminRequest request) {
        // 检查是否已存在超级管理员
        if (adminInitService.hasSuperAdmin()) {
            return Result.error(400, "超级管理员已存在，无法重复初始化");
        }
        
        adminInitService.initSuperAdmin(request);
        return Result.success("超级管理员初始化成功");
    }
    
    /**
     * 创建管理员账号（需要超级管理员权限）
     */
    @PostMapping("/admin")
    @Operation(summary = "创建管理员账号")
    public Result<AdminUser> createAdmin(@Valid @RequestBody InitAdminRequest request,
                                        @RequestParam Long roleId) {
        AdminUser adminUser = adminInitService.createAdmin(request, roleId);
        return Result.success(adminUser);
    }
}
