package com.shortlink.controller;

/**
 * @author LYT0905
 * @date 2024/03/07/12:56
 */


import com.shortlink.common.convention.result.Result;
import com.shortlink.common.convention.result.Results;
import com.shortlink.dto.request.RecycleBinSaveReqDTO;
import com.shortlink.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
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
}
