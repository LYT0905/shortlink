package com.shortlink.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shortlink.dao.entity.ShortLinkDO;
import com.shortlink.dto.request.ShortLinkCreateReqDTO;
import com.shortlink.dto.request.ShortLinkPageReqDTO;
import com.shortlink.dto.response.ShortLinkCreateRespDTO;
import com.shortlink.dto.response.ShortLinkGroupRespDTO;
import com.shortlink.dto.response.ShortLinkPageRespDTO;

import java.util.List;

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

    /**
     * 短链接分页查询
     * @param requestParam 短链接分页查询参数
     * @return 短链接分页查询返回结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 查询当前组下的短链接数量
     * @param requestParam 分组标识
     * @return 短链接数量返回
     */
    List<ShortLinkGroupRespDTO> listGroupShortLinkCount(List<String> requestParam);
}
