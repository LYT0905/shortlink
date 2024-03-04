package com.shortlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.common.convention.exception.ServiceException;
import com.shortlink.common.enums.ShortLinkErrorCodeEnums;
import com.shortlink.dao.entity.ShortLinkDO;
import com.shortlink.dao.mapper.ShortLinkMapper;
import com.shortlink.dto.request.ShortLinkCreateReqDTO;
import com.shortlink.dto.response.ShortLinkCreateRespDTO;
import com.shortlink.service.ShortLinkService;
import com.shortlink.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author LYT0905
 * @date 2024/03/03/21:42
 */

@Service
@RequiredArgsConstructor

public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortLinkUriCachePenetrationBloomFilter;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUri = requestParam.getDomain() + "/" + shortLinkSuffix;
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(requestParam.getDomain())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .fullShortUrl(fullShortUri)
                .build();

        try{
            baseMapper.insert(shortLinkDO);
        }catch (DuplicateKeyException ex){
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUri);
            ShortLinkDO hasShortLink = baseMapper.selectOne(queryWrapper);
            if(hasShortLink != null){
                throw new ServiceException(ShortLinkErrorCodeEnums.SHORT_LINK_INSERT_ERROR);
            }

        }
        shortLinkUriCachePenetrationBloomFilter.add(fullShortUri);
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
    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        int customGenerateCount = 0;
        String shorUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后再试");
            }
            String originUrl = requestParam.getOriginUrl();
            originUrl += UUID.randomUUID().toString();
            shorUri = HashUtil.hashToBase62(originUrl);
            if (!shortLinkUriCachePenetrationBloomFilter.contains(requestParam.getDomain() + "/" + shorUri)) {
                break;
            }
            customGenerateCount++;
        }
        return shorUri;
    }
}
