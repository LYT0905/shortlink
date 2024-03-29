package com.shortlink.project.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortlink.project.common.convention.result.Result;
import com.shortlink.project.common.convention.result.Results;
import com.shortlink.project.dto.request.ShortLinkBatchCreateReqDTO;
import com.shortlink.project.dto.request.ShortLinkCreateReqDTO;
import com.shortlink.project.dto.request.ShortLinkPageReqDTO;
import com.shortlink.project.dto.request.ShortLinkUpdateReqDTO;
import com.shortlink.project.dto.response.ShortLinkBatchCreateRespDTO;
import com.shortlink.project.dto.response.ShortLinkCreateRespDTO;
import com.shortlink.project.dto.response.ShortLinkGroupRespDTO;
import com.shortlink.project.dto.response.ShortLinkPageRespDTO;
import com.shortlink.project.handler.CustomBlockHandler;
import com.shortlink.project.service.ShortLinkService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @author LYT0905
 * @date 2024/03/03/21:41
 */

@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接
     * @param requestParam 短链接创建参数
     * @return ShortLinkCreateRespDTO
     */
    @PostMapping("/api/short-link/v1/create")
    @SentinelResource(
            value = "create_short-link",
            blockHandler = "createShortLinkBlockHandlerMethod",
            blockHandlerClass = CustomBlockHandler.class
    )
    public Result<ShortLinkCreateRespDTO> create(@RequestBody ShortLinkCreateReqDTO requestParam){
        return Results.success(shortLinkService.createShortLink(requestParam));
    }

    /**
     * 批量创建短链接
     */
    @PostMapping("/api/short-link/v1/create/batch")
    public Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam) {
        return Results.success(shortLinkService.batchCreateShortLink(requestParam));
    }

    /**
     * 短链接分页查询
     * @param requestParam 短链接分页查询参数
     * @return 短链接分页查询返回结果
     */
    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> page(ShortLinkPageReqDTO requestParam){
        return Results.success(shortLinkService.pageShortLink(requestParam));
    }

    /**
     * 查询当前组下的短链接数量
     * @param requestParam 分组标识
     * @return 短链接数量返回
     */
    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkGroupRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam){
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParam));
    }

    /**
     * 修改短链接
     * @param requestParam 修改短链接请求参数
     * @return void
     */
    @PostMapping("/api/short-link/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam){
        shortLinkService.updateShortLink(requestParam);
        return Results.success();
    }

    /**
     * 短链接跳转
     * @param shortUri 获取跳转的短链接
     * @param request 请求
     * @param response 响应
     */
    @GetMapping("/{short-uri}")
    public void restoreUri(@PathVariable("short-uri") String shortUri, ServletRequest request, ServletResponse response) throws IOException {
        shortLinkService.restoreUri(shortUri, request, response);
    }
}
