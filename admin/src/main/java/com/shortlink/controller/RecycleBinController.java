package com.shortlink.controller;

/**
 * @author LYT0905
 * @date 2024/03/07/12:56
 */


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.common.convention.result.Result;
import com.shortlink.common.convention.result.Results;
import com.shortlink.remote.ShortLinkActualRemoteService;
import com.shortlink.remote.dto.request.RecycleBinPageReqDTO;
import com.shortlink.remote.dto.request.RecycleBinRecoverReqDTO;
import com.shortlink.remote.dto.request.RecycleBinRemoveReqDTO;
import com.shortlink.remote.dto.request.RecycleBinSaveReqDTO;
import com.shortlink.remote.dto.response.ShortLinkPageRespDTO;
import com.shortlink.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接回收站
 */

@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    private final RecycleBinService recycleBinService;

    /**
     * 保存回收站
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        shortLinkActualRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 回收站短链接分页查询
     * @param requestParam 短链接分页查询参数
     * @return 短链接分页查询返回结果
     */
    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<Page<ShortLinkPageRespDTO>> page(RecycleBinPageReqDTO requestParam){
        return recycleBinService.pageRecycleShortLink(requestParam);
    }

    /**
     * 回收站短链接恢复功能
     * @param requestParam 请求参数
     * @return void
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/recover")
    public Result<Void> recover(@RequestBody RecycleBinRecoverReqDTO requestParam){
        shortLinkActualRemoteService.recoverRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 回收站中的短链接删除
     * @param requestParam 请求参数
     * @return void
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/remove")
    public Result<Void> remove(@RequestBody RecycleBinRemoveReqDTO requestParam){
        shortLinkActualRemoteService.removeRecycleBin(requestParam);
        return Results.success();
    }
}
