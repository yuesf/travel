package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.ProductCategoryCreateRequest;
import com.travel.dto.ProductCategoryUpdateRequest;
import com.travel.entity.ProductCategory;
import com.travel.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 商品分类管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/product-categories")
@Tag(name = "商品分类管理")
public class ProductCategoryController {
    
    @Autowired
    private ProductCategoryService productCategoryService;
    
    /**
     * 查询所有分类列表
     */
    @GetMapping
    @Operation(summary = "查询分类列表")
    public Result<List<ProductCategory>> list(@RequestParam(required = false) Integer status,
                                               @RequestParam(required = false) String type) {
        List<ProductCategory> list = productCategoryService.list(status, type);
        return Result.success(list);
    }
    
    /**
     * 查询分类树（一级分类及其子分类）
     */
    @GetMapping("/tree")
    @Operation(summary = "查询分类树")
    public Result<List<ProductCategory>> getTree(@RequestParam(required = false) Integer status,
                                                  @RequestParam(required = false) String type) {
        List<ProductCategory> tree = productCategoryService.getTree(status, type);
        return Result.success(tree);
    }
    
    /**
     * 根据ID查询分类详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询分类详情")
    public Result<ProductCategory> getById(@PathVariable Long id) {
        ProductCategory category = productCategoryService.getById(id);
        return Result.success(category);
    }
    
    /**
     * 创建分类
     */
    @PostMapping
    @Operation(summary = "创建分类")
    public Result<ProductCategory> create(@Valid @RequestBody ProductCategoryCreateRequest request) {
        ProductCategory category = productCategoryService.create(request);
        return Result.success(category);
    }
    
    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新分类")
    public Result<ProductCategory> update(@PathVariable Long id, 
                                         @Valid @RequestBody ProductCategoryUpdateRequest request) {
        ProductCategory category = productCategoryService.update(id, request);
        return Result.success(category);
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<?> delete(@PathVariable Long id) {
        productCategoryService.delete(id);
        return Result.success();
    }
}
