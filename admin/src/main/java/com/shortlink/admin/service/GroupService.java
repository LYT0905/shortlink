package com.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortlink.admin.dao.entity.GroupDO;
import com.shortlink.admin.dto.request.ShortLinkGroupUpdateReqDTO;
import com.shortlink.admin.dto.request.ShortLinkGroupUpdateSortReqDTO;
import com.shortlink.admin.dto.response.ShortLinkGroupRespDTO;

import java.util.List;

/**
 * @author LYT0905
 * @date 2024/03/01/16:05
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组
     * @param groupName 请求参数
     * @return void
     */
    void saveGroup(String groupName);

    /**
     * 新增短链接分组
     * @param username 用户名
     * @param groupName 请求参数
     * @return void
     */
    void saveGroup(String username, String groupName);

    /**
     * 查询短链接分组
     * @return 短链接分组
     */
    List<ShortLinkGroupRespDTO> lisGroup();

    /**
     * 修改短链接分组名称
     * @param requestParam 修改名称与修改对象
     * @return void
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);

    /**
     * 短链接分组删除
     * @param gid 短链接分组标识
     * @return
     */
    void deleteGroup(String gid);

    /**
     * 修改短链接分组排序
     * @param requestParam
     * @return
     */
    void updateGroupSort(List<ShortLinkGroupUpdateSortReqDTO> requestParam);
}
