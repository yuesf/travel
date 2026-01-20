package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.LoginRequest;
import com.travel.dto.LoginResponse;
import com.travel.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


/**
 * 管理员认证控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController("adminAuthController")
@RequestMapping("/api/v1/admin/auth")
@Tag(name = "管理员认证")
public class AuthController {
    
    @Autowired
    private AdminAuthService adminAuthService;
    
    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginResponse response = adminAuthService.login(request, httpRequest);
        return Result.success(response);
    }
    
    /**
     * 管理员登出
     */
    @PostMapping("/logout")
    @Operation(summary = "管理员登出")
    public Result<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        adminAuthService.logout(token);
        return Result.success();
    }
}
