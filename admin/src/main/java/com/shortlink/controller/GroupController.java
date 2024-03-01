package com.shortlink.controller;

import com.shortlink.common.convention.result.Result;
import com.shortlink.common.convention.result.Results;
import com.shortlink.dto.request.ShortLinkGroupSaveReqDTO;
import com.shortlink.dto.request.ShortLinkGroupUpdateReqDTO;
import com.shortlink.dto.request.ShortLinkGroupUpdateSortReqDTO;
import com.shortlink.dto.response.ShortLinkGroupRespDTO;
import com.shortlink.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 查询短链接分组
     * @return 短链接分组
     */
    @GetMapping("/api/short-link/admin/v1/group")
    public Result<List<ShortLinkGroupRespDTO>> listGroup(){
        return Results.success(groupService.lisGroup());
    }

    /**
     * 修改短链接分组名称
     * @param requestParam 修改名称与修改对象
     * @return void
     */
    @PutMapping("/api/short-link/admin/v1/group")
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupUpdateReqDTO requestParam){
        groupService.updateGroup(requestParam);
        return Results.success();
    }

    /**
     * 短链接分组删除
     * @param gid 短链接分组标识
     * @return
     */
    @DeleteMapping("/api/short-link/admin/v1/group")
    public Result<Void> deleteGroup(@RequestParam("gid") String gid){
        groupService.deleteGroup(gid);
        return Results.success();
    }

    /**
     * 修改短链接分组排序
     * @param requestParam
     * @return
     */
    @PutMapping("/api/short-link/admin/v1/group/sort")
    public Result<Void> updateGroupSort(@RequestBody List<ShortLinkGroupUpdateSortReqDTO> requestParam){
        groupService.updateGroupSort(requestParam);
        return Results.success();
    }
}
