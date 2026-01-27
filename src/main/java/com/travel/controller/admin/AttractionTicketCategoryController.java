package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.AttractionTicketCategoryCreateRequest;
import com.travel.dto.AttractionTicketCategoryUpdateRequest;
import com.travel.entity.AttractionTicketCategory;
import com.travel.service.AttractionTicketCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 票种分类管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/attractions/{attractionId}/ticket-categories")
@Tag(name = "票种分类管理")
public class AttractionTicketCategoryController {
    
    @Autowired
    private AttractionTicketCategoryService categoryService;
    
    /**
     * 查询票种分类列表
     */
    @GetMapping
    @Operation(summary = "查询票种分类列表")
    public Result<List<AttractionTicketCategory>> list(@PathVariable Long attractionId) {
        List<AttractionTicketCategory> list = categoryService.list(attractionId);
        return Result.success(list);
    }
    
    /**
     * 根据ID查询票种分类详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询票种分类详情")
    public Result<AttractionTicketCategory> getById(@PathVariable Long id) {
        AttractionTicketCategory category = categoryService.getById(id);
        return Result.success(category);
    }
    
    /**
     * 创建票种分类
     */
    @PostMapping
    @Operation(summary = "创建票种分类")
    public Result<AttractionTicketCategory> create(
            @PathVariable Long attractionId,
            @Valid @RequestBody AttractionTicketCategoryCreateRequest request) {
        // 先设置景点ID，再验证（如果需要验证attractionId的话）
        request.setAttractionId(attractionId);
        AttractionTicketCategory category = categoryService.create(request);
        return Result.success(category);
    }
    
    /**
     * 更新票种分类
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新票种分类")
    public Result<AttractionTicketCategory> update(
            @PathVariable Long id,
            @Valid @RequestBody AttractionTicketCategoryUpdateRequest request) {
        AttractionTicketCategory category = categoryService.update(id, request);
        return Result.success(category);
    }
    
    /**
     * 删除票种分类
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除票种分类")
    public Result<?> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
