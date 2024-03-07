package com.shortlink.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortlink.common.convention.result.Result;
import com.shortlink.common.convention.result.Results;
import com.shortlink.remote.dto.ShortLinkRemoteService;
import com.shortlink.remote.dto.request.ShortLinkCreateReqDTO;
import com.shortlink.remote.dto.request.ShortLinkPageReqDTO;
import com.shortlink.remote.dto.request.ShortLinkUpdateReqDTO;
import com.shortlink.remote.dto.response.ShortLinkCreateRespDTO;
import com.shortlink.remote.dto.response.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author LYT0905
 * @date 2024/03/05/12:47
 */


/**
 * 短链接后管控制层
 */
@RestController
public class ShortLinkController {

    // TODO 后续重构为spring cloud远程调用


    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 创建短链接
     * @param requestParam 短链接创建参数
     * @return ShortLinkCreateRespDTO
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> create(@RequestBody ShortLinkCreateReqDTO requestParam){
        return shortLinkRemoteService.createShortLink(requestParam);
    }

    /**
     * 短链接分页查询
     * @param requestParam 短链接分页查询参数
     * @return 短链接分页查询返回结果
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> page(ShortLinkPageReqDTO requestParam){
        return shortLinkRemoteService.pageShortLink(requestParam);
    }

    /**
     * 修改短链接
     * @param requestParam 修改短链接请求参数
     * @return void
     */
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam){
        shortLinkRemoteService.updateShortLink(requestParam);
        return Results.success();
    }
}
