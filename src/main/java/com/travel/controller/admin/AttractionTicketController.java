package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.AttractionTicketCreateRequest;
import com.travel.dto.AttractionTicketUpdateRequest;
import com.travel.entity.AttractionTicket;
import com.travel.service.AttractionTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 具体票种管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/attractions/{attractionId}/tickets")
@Tag(name = "具体票种管理")
public class AttractionTicketController {
    
    @Autowired
    private AttractionTicketService ticketService;
    
    /**
     * 查询票种列表
     */
    @GetMapping
    @Operation(summary = "查询票种列表")
    public Result<List<AttractionTicket>> list(
            @PathVariable Long attractionId,
            @RequestParam(required = false) Long categoryId) {
        List<AttractionTicket> list = ticketService.list(attractionId, categoryId);
        return Result.success(list);
    }
    
    /**
     * 根据ID查询票种详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询票种详情")
    public Result<AttractionTicket> getById(@PathVariable Long id) {
        AttractionTicket ticket = ticketService.getById(id);
        return Result.success(ticket);
    }
    
    /**
     * 创建票种
     */
    @PostMapping
    @Operation(summary = "创建票种")
    public Result<AttractionTicket> create(
            @PathVariable Long attractionId,
            @Valid @RequestBody AttractionTicketCreateRequest request) {
        request.setAttractionId(attractionId);
        AttractionTicket ticket = ticketService.create(request);
        return Result.success(ticket);
    }
    
    /**
     * 更新票种
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新票种")
    public Result<AttractionTicket> update(
            @PathVariable Long id,
            @Valid @RequestBody AttractionTicketUpdateRequest request) {
        AttractionTicket ticket = ticketService.update(id, request);
        return Result.success(ticket);
    }
    
    /**
     * 删除票种
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除票种")
    public Result<?> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return Result.success();
    }
}
