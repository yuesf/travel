package com.travel.controller.miniprogram;

import com.travel.common.Result;
import com.travel.common.ResultCode;
import com.travel.entity.User;
import com.travel.service.UserAuthService;
import com.travel.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序用户控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/miniprogram/user")
@Tag(name = "小程序用户")
public class UserController {
    
    @Autowired
    private UserAuthService userAuthService;
    
    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取用户信息")
    public Result<User> getUserInfo() {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        User user = userAuthService.getUserInfo(userId);
        return Result.success(user);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    @Operation(summary = "更新用户信息")
    public Result<?> updateUserInfo(@RequestBody User userInfo) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        userAuthService.updateUserInfo(userId, userInfo);
        return Result.success("更新成功");
    }
}
