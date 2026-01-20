package com.travel.controller.miniprogram;

import com.travel.common.Result;
import com.travel.dto.CategoryListRequest;
import com.travel.dto.PageResult;
import com.travel.entity.Attraction;
import com.travel.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 小程序分类控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/miniprogram")
@Tag(name = "小程序分类")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * 获取分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取分类列表")
    public Result<List<Map<String, Object>>> getCategories() {
        List<Map<String, Object>> categories = categoryService.getCategoryList();
        return Result.success(categories);
    }
    
    /**
     * 获取景点列表（支持分类筛选）
     */
    @GetMapping("/attractions")
    @Operation(summary = "获取景点列表")
    public Result<PageResult<Attraction>> getAttractions(CategoryListRequest request) {
        PageResult<Attraction> result = categoryService.getAttractionList(request);
        return Result.success(result);
    }
    
    /**
     * 获取酒店列表（支持分类筛选）
     */
    @GetMapping("/hotels")
    @Operation(summary = "获取酒店列表")
    public Result<PageResult<Map<String, Object>>> getHotels(CategoryListRequest request) {
        PageResult<Map<String, Object>> result = categoryService.getHotelList(request);
        return Result.success(result);
    }
    
    /**
     * 获取商品列表（支持分类筛选）
     */
    @GetMapping("/products")
    @Operation(summary = "获取商品列表")
    public Result<PageResult<Map<String, Object>>> getProducts(
            CategoryListRequest request,
            @RequestParam(required = false) String sort) {
        
        // 解析sort参数（格式：price_asc, price_desc, sales_desc等）
        if (sort != null && !sort.isEmpty() && !"default".equals(sort)) {
            String[] sortParts = sort.split("_");
            if (sortParts.length >= 2) {
                String field = sortParts[0];
                String order = sortParts[1];
                
                // 映射前端字段名到后端字段名
                if ("price".equals(field)) {
                    request.setSortField("price");
                } else if ("sales".equals(field)) {
                    request.setSortField("sales");
                } else if ("rating".equals(field)) {
                    request.setSortField("rating");
                } else {
                    request.setSortField("create_time");
                }
                
                if ("asc".equalsIgnoreCase(order)) {
                    request.setSortOrder("asc");
                } else {
                    request.setSortOrder("desc");
                }
            }
        }
        
        PageResult<Map<String, Object>> result = categoryService.getProductList(request);
        return Result.success(result);
    }
}
