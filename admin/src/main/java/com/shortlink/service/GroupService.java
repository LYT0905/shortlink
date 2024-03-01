package com.shortlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortlink.dao.entity.GroupDO;

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
}
