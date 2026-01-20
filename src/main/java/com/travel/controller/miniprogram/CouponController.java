package com.travel.controller.miniprogram;

import com.travel.common.Result;
import com.travel.common.ResultCode;
import com.travel.dto.PageResult;
import com.travel.entity.UserCoupon;
import com.travel.service.CouponService;
import com.travel.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序优惠券控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/miniprogram/coupons")
@Tag(name = "小程序优惠券")
public class CouponController {
    
    @Autowired
    private CouponService couponService;
    
    /**
     * 获取用户优惠券列表（按状态分类）
     */
    @GetMapping
    @Operation(summary = "获取用户优惠券列表")
    public Result<PageResult<UserCoupon>> getCouponList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        PageResult<UserCoupon> result = couponService.getUserCoupons(userId, status, page, pageSize);
        return Result.success(result);
    }
}
