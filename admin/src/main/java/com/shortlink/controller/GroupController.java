package com.shortlink.controller;

import com.shortlink.common.convention.result.Result;
import com.shortlink.common.convention.result.Results;
import com.shortlink.dto.request.ShortLinkGroupSaveReqDTO;
import com.shortlink.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author LYT0905
 * @date 2024/03/01/16:07
 */

@RestController
@RequiredArgsConstructor
public class GroupController{

    private final GroupService groupService;

    /**
     * 新增短链接分组
     * @param requestParam 请求参数
     * @return void
     */
    @PostMapping("/api/short-link/admin/v1/group")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO requestParam){
        groupService.saveGroup(requestParam.getName());
        return Results.success();
    }
}
