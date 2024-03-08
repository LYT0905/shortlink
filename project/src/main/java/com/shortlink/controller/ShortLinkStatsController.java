package com.shortlink.controller;

/**
 * @author LYT0905
 * @date 2024/03/08/20:13
 */

import com.shortlink.common.convention.result.Result;
import com.shortlink.common.convention.result.Results;
import com.shortlink.dto.request.ShortLinkStatsReqDTO;
import com.shortlink.dto.response.ShortLinkStatsRespDTO;
import com.shortlink.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkStatsService shortLinkStatsService;

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/v1/stats")
    public Result<ShortLinkStatsRespDTO> stats(ShortLinkStatsReqDTO requestParam){
        return Results.success(shortLinkStatsService.oneShortLinkStats(requestParam));
    }
}