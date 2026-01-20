package com.travel.controller.miniprogram;

import com.travel.common.Result;
import com.travel.common.ResultCode;
import com.travel.entity.UserAddress;
import com.travel.service.UserAddressService;
import com.travel.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序收货地址控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/miniprogram/addresses")
@Tag(name = "小程序收货地址")
public class UserAddressController {
    
    @Autowired
    private UserAddressService userAddressService;
    
    /**
     * 获取收货地址列表
     */
    @GetMapping
    @Operation(summary = "获取收货地址列表")
    public Result<List<UserAddress>> getAddressList() {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        List<UserAddress> addressList = userAddressService.getAddressList(userId);
        return Result.success(addressList);
    }
    
    /**
     * 获取收货地址详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取收货地址详情")
    public Result<UserAddress> getAddressById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        UserAddress address = userAddressService.getAddressById(id, userId);
        return Result.success(address);
    }
    
    /**
     * 创建收货地址
     */
    @PostMapping
    @Operation(summary = "创建收货地址")
    public Result<UserAddress> createAddress(@RequestBody UserAddress address) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        UserAddress createdAddress = userAddressService.createAddress(userId, address);
        return Result.success(createdAddress);
    }
    
    /**
     * 更新收货地址
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新收货地址")
    public Result<UserAddress> updateAddress(@PathVariable Long id, @RequestBody UserAddress address) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        UserAddress updatedAddress = userAddressService.updateAddress(userId, id, address);
        return Result.success(updatedAddress);
    }
    
    /**
     * 删除收货地址
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除收货地址")
    public Result<?> deleteAddress(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        userAddressService.deleteAddress(userId, id);
        return Result.success("删除成功");
    }
    
    /**
     * 设置默认收货地址
     */
    @PutMapping("/{id}/default")
    @Operation(summary = "设置默认收货地址")
    public Result<?> setDefaultAddress(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        userAddressService.setDefaultAddress(userId, id);
        return Result.success("设置成功");
    }
}
