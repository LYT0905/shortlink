package com.shortlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortlink.dao.entity.ShortLinkDO;
import com.shortlink.dto.request.ShortLinkCreateReqDTO;
import com.shortlink.dto.response.ShortLinkCreateRespDTO;

/**
 * @author LYT0905
 * @date 2024/03/03/21:41
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 创建短链接
     * @param requestParam 短链接创建参数
     * @return ShortLinkCreateRespDTO
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);
}
