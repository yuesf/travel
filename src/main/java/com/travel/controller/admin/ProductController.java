package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.PageResult;
import com.travel.dto.ProductCreateRequest;
import com.travel.dto.ProductListRequest;
import com.travel.dto.ProductUpdateRequest;
import com.travel.entity.Product;
import com.travel.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 商品管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/products")
@Tag(name = "商品管理")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    /**
     * 分页查询商品列表
     */
    @GetMapping
    @Operation(summary = "分页查询商品列表")
    public Result<PageResult<Product>> list(ProductListRequest request) {
        PageResult<Product> result = productService.list(request);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询商品详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询商品详情")
    public Result<Product> getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        return Result.success(product);
    }
    
    /**
     * 创建商品
     */
    @PostMapping
    @Operation(summary = "创建商品")
    public Result<Product> create(@Valid @RequestBody ProductCreateRequest request) {
        Product product = productService.create(request);
        return Result.success(product);
    }
    
    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新商品")
    public Result<Product> update(@PathVariable Long id, 
                                 @Valid @RequestBody ProductUpdateRequest request) {
        Product product = productService.update(id, request);
        return Result.success(product);
    }
    
    /**
     * 删除商品（软删除，改为下架）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    public Result<?> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }
    
    /**
     * 更新库存
     */
    @PutMapping("/{id}/stock")
    @Operation(summary = "更新商品库存")
    public Result<?> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        productService.updateStock(id, stock);
        return Result.success();
    }
}
