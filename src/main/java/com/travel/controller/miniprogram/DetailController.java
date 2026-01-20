package com.travel.controller.miniprogram;

import com.travel.common.Result;
import com.travel.entity.Attraction;
import com.travel.service.DetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 小程序详情控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/miniprogram")
@Tag(name = "小程序详情")
public class DetailController {
    
    @Autowired
    private DetailService detailService;
    
    /**
     * 获取景点详情
     */
    @GetMapping("/attractions/{id}")
    @Operation(summary = "获取景点详情")
    public Result<Attraction> getAttractionDetail(@PathVariable Long id) {
        Attraction attraction = detailService.getAttractionDetail(id);
        return Result.success(attraction);
    }
    
    /**
     * 获取酒店详情（包含房型列表）
     */
    @GetMapping("/hotels/{id}")
    @Operation(summary = "获取酒店详情")
    public Result<Map<String, Object>> getHotelDetail(@PathVariable Long id) {
        Map<String, Object> hotel = detailService.getHotelDetail(id);
        return Result.success(hotel);
    }
    
    /**
     * 获取商品详情
     */
    @GetMapping("/products/{id}")
    @Operation(summary = "获取商品详情")
    public Result<Map<String, Object>> getProductDetail(@PathVariable Long id) {
        Map<String, Object> product = detailService.getProductDetail(id);
        return Result.success(product);
    }
}
