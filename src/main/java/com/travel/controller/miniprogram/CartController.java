package com.travel.controller.miniprogram;

import com.travel.common.Result;
import com.travel.common.ResultCode;
import com.travel.dto.CartAddRequest;
import com.travel.dto.CartResponse;
import com.travel.dto.CartUpdateRequest;
import com.travel.entity.Cart;
import com.travel.service.CartService;
import com.travel.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 小程序购物车控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/miniprogram/cart")
@Tag(name = "小程序购物车")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    /**
     * 获取购物车列表（包含商品详情）
     * 每次调用都从数据库实时查询，不使用缓存
     */
    @GetMapping
    @Operation(summary = "获取购物车列表（云购物车方案）")
    public Result<List<CartResponse>> getCartList() {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        // 从数据库实时查询购物车列表（包含商品详情）
        List<CartResponse> cartList = cartService.getCartList(userId);
        return Result.success(cartList);
    }
    
    /**
     * 添加商品到购物车
     */
    @PostMapping
    @Operation(summary = "添加商品到购物车")
    public Result<Cart> addToCart(@Valid @RequestBody CartAddRequest request) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        Cart cart = cartService.addToCart(userId, request);
        return Result.success(cart);
    }
    
    /**
     * 更新购物车商品数量
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新购物车商品数量")
    public Result<Cart> updateCart(@PathVariable Long id,
                                   @Valid @RequestBody CartUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        Cart cart = cartService.updateCart(userId, id, request);
        return Result.success(cart);
    }
    
    /**
     * 删除购物车商品
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除购物车商品")
    public Result<?> deleteCart(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        cartService.deleteCart(userId, id);
        return Result.success();
    }
}
