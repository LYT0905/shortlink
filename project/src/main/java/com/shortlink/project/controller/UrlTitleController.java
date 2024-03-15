package com.shortlink.project.controller;

import com.shortlink.project.common.convention.result.Result;
import com.shortlink.project.common.convention.result.Results;
import com.shortlink.project.service.UrlTitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author LYT0905
 * @date 2024/03/06/21:38
 */


@RestController
@RequiredArgsConstructor
public class UrlTitleController {

    private final UrlTitleService urlTitleService;

    /**
     * 根据链接获取标题
     * @param url 链接
     * @return 链接标题
     */
    @GetMapping("/api/short-link/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url){
        return Results.success(urlTitleService.getTitleByUrl(url));
    }
}
