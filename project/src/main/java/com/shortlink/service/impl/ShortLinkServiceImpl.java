package com.shortlink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.dao.entity.ShortLinkDO;
import com.shortlink.dao.mapper.ShortLinkMapper;
import com.shortlink.dto.request.ShortLinkCreateReqDTO;
import com.shortlink.dto.response.ShortLinkCreateRespDTO;
import com.shortlink.service.ShortLinkService;
import com.shortlink.toolkit.HashUtil;
import org.springframework.stereotype.Service;

/**
 * @author LYT0905
 * @date 2024/03/03/21:42
 */

@Service
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        ShortLinkDO shortLinkDO = BeanUtil.toBean(requestParam, ShortLinkDO.class);
        shortLinkDO.setEnableStatus(0);
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setFullShortUrl(requestParam.getDomain() + "/" + shortLinkSuffix);
        baseMapper.insert(shortLinkDO);
        return ShortLinkCreateRespDTO
                .builder()
                .gid(requestParam.getGid())
                .shortUri(shortLinkSuffix)
                .originUrl(requestParam.getOriginUrl())
                .build();
    }

    /**
     * 构建短链接前缀
     * @param requestParam 构建参数
     * @return 构建返回对象
     */
    private String generateSuffix(ShortLinkCreateReqDTO requestParam){
        String originUrl = requestParam.getOriginUrl();
        return HashUtil.hashToBase62(originUrl);
    }
}
