package com.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shortlink.project.dao.entity.ShortLinkDO;
import com.shortlink.project.dto.request.RecycleBinPageReqDTO;
import com.shortlink.project.dto.request.RecycleBinRecoverReqDTO;
import com.shortlink.project.dto.request.RecycleBinRemoveReqDTO;
import com.shortlink.project.dto.request.RecycleBinSaveReqDTO;
import com.shortlink.project.dto.response.ShortLinkPageRespDTO;

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

    /**
     * 短链接分页查询
     * @param requestParam 短链接分页查询参数
     * @return 短链接分页查询返回结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(RecycleBinPageReqDTO requestParam);

    /**
     * 回收站短链接恢复功能
     * @param requestParam 请求参数
     * @return void
     */
    void recoverShortLinkRecycleBin(RecycleBinRecoverReqDTO requestParam);

    /**
     * 回收站中的短链接删除
     * @param requestParam 请求参数
     * @return void
     */
    void removeShortLinkRecycleBin(RecycleBinRemoveReqDTO requestParam);
}
