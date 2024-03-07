package com.shortlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortlink.dao.entity.ShortLinkDO;
import com.shortlink.dto.request.RecycleBinSaveReqDTO;

/**
 * @author LYT0905
 * @date 2024/03/07/12:59
 */

/**
 * 回收站服务层
 */
public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 将短链接移至到回收站
     * @param requestParam 请求参数
     * @return void
     */
    void recycleBinSave(RecycleBinSaveReqDTO requestParam);
}
