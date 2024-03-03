package com.shortlink.controller;

import com.shortlink.common.convention.result.Result;
import com.shortlink.common.convention.result.Results;
import com.shortlink.dto.request.ShortLinkCreateReqDTO;
import com.shortlink.dto.response.ShortLinkCreateRespDTO;
import com.shortlink.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> create(@RequestBody ShortLinkCreateReqDTO requestParam){
        return Results.success(shortLinkService.createShortLink(requestParam));
    }
}
