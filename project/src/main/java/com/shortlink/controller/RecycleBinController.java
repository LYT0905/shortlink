package com.shortlink.controller;

/**
 * @author LYT0905
 * @date 2024/03/07/12:56
 */


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortlink.common.convention.result.Result;
import com.shortlink.common.convention.result.Results;
import com.shortlink.dto.request.RecycleBinPageReqDTO;
import com.shortlink.dto.request.RecycleBinRecoverDTO;
import com.shortlink.dto.request.RecycleBinSaveReqDTO;
import com.shortlink.dto.response.ShortLinkPageRespDTO;
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

    private final RecycleBinService recycleBinService;

    /**
     * 将短链接移至到回收站
     * @param requestParam 请求参数
     * @return void
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> recycleBinSave(@RequestBody RecycleBinSaveReqDTO requestParam){
        recycleBinService.recycleBinSave(requestParam);
        return Results.success();
    }


    /**
     * 回收站短链接分页查询
     * @param requestParam 短链接分页查询参数
     * @return 短链接分页查询返回结果
     */
    @GetMapping("/api/short-link/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> page(RecycleBinPageReqDTO requestParam){
        return Results.success(recycleBinService.pageShortLink(requestParam));
    }

    /**
     * 回收站短链接恢复功能
     * @param requestParam 请求参数
     * @return void
     */
    @PostMapping("/api/short-link/v1/recycle-bin/recover")
    public Result<Void> recover(@RequestBody RecycleBinRecoverDTO requestParam){
        recycleBinService.recoverShortLinkRecycleBin(requestParam);
        return Results.success();
    }
}
