package com.shortlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.common.convention.result.Result;
import com.shortlink.common.convention.result.Results;
import com.shortlink.remote.ShortLinkActualRemoteService;
import com.shortlink.remote.dto.request.ShortLinkBatchCreateReqDTO;
import com.shortlink.remote.dto.request.ShortLinkCreateReqDTO;
import com.shortlink.remote.dto.request.ShortLinkPageReqDTO;
import com.shortlink.remote.dto.request.ShortLinkUpdateReqDTO;
import com.shortlink.remote.dto.response.ShortLinkBaseInfoRespDTO;
import com.shortlink.remote.dto.response.ShortLinkBatchCreateRespDTO;
import com.shortlink.remote.dto.response.ShortLinkCreateRespDTO;
import com.shortlink.remote.dto.response.ShortLinkPageRespDTO;
import com.shortlink.toolkit.EasyExcelWebUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author LYT0905
 * @date 2024/03/05/12:47
 */


/**
 * 短链接后管控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 创建短链接
     * @param requestParam 短链接创建参数
     * @return ShortLinkCreateRespDTO
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> create(@RequestBody ShortLinkCreateReqDTO requestParam){
        return shortLinkActualRemoteService.createShortLink(requestParam);
    }

    /**
     * 批量创建短链接
     */
    @SneakyThrows
    @PostMapping("/api/short-link/admin/v1/create/batch")
    public void batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam, HttpServletResponse response){
        Result<ShortLinkBatchCreateRespDTO> shortLinkBatchCreateRespDTOResult = shortLinkActualRemoteService.batchCreateShortLink(requestParam);
        // 如果成功就写出一份excel文件，浏览器自动下载
        if(shortLinkBatchCreateRespDTOResult.isSuccess()){
            List<ShortLinkBaseInfoRespDTO> baseLinkInfos = shortLinkBatchCreateRespDTOResult.getData().getBaseLinkInfos();
            EasyExcelWebUtil.write(response,  "批量创建短链接-SaaS短链接系统", ShortLinkBaseInfoRespDTO.class, baseLinkInfos);
        }
    }

    /**
     * 短链接分页查询
     * @param requestParam 短链接分页查询参数
     * @return 短链接分页查询返回结果
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<Page<ShortLinkPageRespDTO>> page(ShortLinkPageReqDTO requestParam){
        return shortLinkActualRemoteService.pageShortLink(requestParam.getGid(), requestParam.getOrderTag(), requestParam.getCurrent(), requestParam.getSize());
    }

    /**
     * 修改短链接
     * @param requestParam 修改短链接请求参数
     * @return void
     */
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam){
        shortLinkActualRemoteService.updateShortLink(requestParam);
        return Results.success();
    }
}
