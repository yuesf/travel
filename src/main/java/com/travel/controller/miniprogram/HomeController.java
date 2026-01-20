package com.travel.controller.miniprogram;

import com.travel.common.Result;
import com.travel.dto.HomeResponse;
import com.travel.dto.PageResult;
import com.travel.dto.SearchRequest;
import com.travel.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序首页控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/miniprogram/home")
@Tag(name = "小程序首页")
public class HomeController {
    
    @Autowired
    private HomeService homeService;
    
    /**
     * 获取首页数据
     */
    @GetMapping
    @Operation(summary = "获取首页数据")
    public Result<HomeResponse> getHome() {
        log.info("小程序请求首页数据");
        HomeResponse homeData = homeService.getHomeData();
        log.info("返回首页数据 - 轮播图数量: {}, Icon数量: {}, 推荐景点数量: {}", 
            homeData.getBanners() != null ? homeData.getBanners().size() : 0,
            homeData.getIcons() != null ? homeData.getIcons().size() : 0,
            homeData.getRecommendAttractions() != null ? homeData.getRecommendAttractions().size() : 0);
        return Result.success(homeData);
    }
    
    /**
     * 搜索接口
     */
    @GetMapping("/search")
    @Operation(summary = "搜索景点、酒店、商品")
    public Result<PageResult<Object>> search(SearchRequest request) {
        PageResult<Object> result = homeService.search(request);
        return Result.success(result);
    }
}
