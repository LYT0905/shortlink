package com.shortlink.controller;

/**
 * @author LYT0905
 * @date 2024/03/08/20:13
 */

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortlink.common.convention.result.Result;
import com.shortlink.remote.dto.ShortLinkRemoteService;
import com.shortlink.remote.dto.request.ShortLinkGroupStatsReqDTO;
import com.shortlink.remote.dto.request.ShortLinkStatsAccessRecordReqDTO;
import com.shortlink.remote.dto.request.ShortLinkStatsReqDTO;
import com.shortlink.remote.dto.response.ShortLinkStatsAccessRecordRespDTO;
import com.shortlink.remote.dto.response.ShortLinkStatsRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> stats(ShortLinkStatsReqDTO requestParam){
        return shortLinkRemoteService.oneShortLinkStats(requestParam);
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return shortLinkRemoteService.shortLinkStatsAccessRecord(requestParam);
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        return shortLinkRemoteService.groupShortLinkStats(requestParam);
    }
}
