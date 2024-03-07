package com.shortlink.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortlink.common.convention.result.Result;
import com.shortlink.remote.dto.request.RecycleBinPageReqDTO;
import com.shortlink.remote.dto.response.ShortLinkPageRespDTO;

/**
 * @author LYT0905
 * @date 2024/03/07/16:45
 */
public interface RecycleBinService {
    Result<IPage<ShortLinkPageRespDTO>> pageRecycleShortLink(RecycleBinPageReqDTO requestParam);
}
