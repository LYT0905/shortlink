package com.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.admin.common.convention.result.Result;
import com.shortlink.admin.remote.dto.request.RecycleBinPageReqDTO;
import com.shortlink.admin.remote.dto.response.ShortLinkPageRespDTO;

/**
 * @author LYT0905
 * @date 2024/03/07/16:45
 */
public interface RecycleBinService {
    Result<Page<ShortLinkPageRespDTO>> pageRecycleShortLink(RecycleBinPageReqDTO requestParam);
}
