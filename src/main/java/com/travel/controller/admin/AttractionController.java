package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.AttractionCreateRequest;
import com.travel.dto.AttractionListRequest;
import com.travel.dto.AttractionUpdateRequest;
import com.travel.dto.PageResult;
import com.travel.entity.Attraction;
import com.travel.service.AttractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 景点管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/attractions")
@Tag(name = "景点管理")
public class AttractionController {
    
    @Autowired
    private AttractionService attractionService;
    
    /**
     * 分页查询景点列表
     */
    @GetMapping
    @Operation(summary = "分页查询景点列表")
    public Result<PageResult<Attraction>> list(AttractionListRequest request) {
        PageResult<Attraction> result = attractionService.list(request);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询景点详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询景点详情")
    public Result<Attraction> getById(@PathVariable Long id) {
        Attraction attraction = attractionService.getById(id);
        return Result.success(attraction);
    }
    
    /**
     * 创建景点
     */
    @PostMapping
    @Operation(summary = "创建景点")
    public Result<Attraction> create(@Valid @RequestBody AttractionCreateRequest request) {
        Attraction attraction = attractionService.create(request);
        return Result.success(attraction);
    }
    
    /**
     * 更新景点
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新景点")
    public Result<Attraction> update(@PathVariable Long id, 
                                     @Valid @RequestBody AttractionUpdateRequest request) {
        Attraction attraction = attractionService.update(id, request);
        return Result.success(attraction);
    }
    
    /**
     * 删除景点（软删除，改为下架）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除景点")
    public Result<?> delete(@PathVariable Long id) {
        attractionService.delete(id);
        return Result.success();
    }
}
