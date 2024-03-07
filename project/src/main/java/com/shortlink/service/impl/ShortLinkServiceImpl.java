package com.shortlink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.common.convention.exception.ClientException;
import com.shortlink.common.convention.exception.ServiceException;
import com.shortlink.common.enums.ShortLinkErrorCodeEnums;
import com.shortlink.common.enums.VailDateTypeEnum;
import com.shortlink.dao.entity.ShortLinkDO;
import com.shortlink.dao.entity.ShortLinkGotoDO;
import com.shortlink.dao.mapper.ShortLinkGotoMapper;
import com.shortlink.dao.mapper.ShortLinkMapper;
import com.shortlink.dto.request.ShortLinkCreateReqDTO;
import com.shortlink.dto.request.ShortLinkPageReqDTO;
import com.shortlink.dto.request.ShortLinkUpdateReqDTO;
import com.shortlink.dto.response.ShortLinkCreateRespDTO;
import com.shortlink.dto.response.ShortLinkGroupRespDTO;
import com.shortlink.dto.response.ShortLinkPageRespDTO;
import com.shortlink.service.ShortLinkService;
import com.shortlink.toolkit.HashUtil;
import com.shortlink.toolkit.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.shortlink.common.constant.RedisKeyConstant.*;

/**
 * @author LYT0905
 * @date 2024/03/03/21:42
 */

@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortLinkUriCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    /**
     * 创建短链接的同时插入到goto表方便定位查找
     * @param requestParam 短链接创建参数
     * @return ShortLinkCreateRespDTO
     */
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
                .favicon(getFavicon(requestParam.getOriginUrl()))
                .enableStatus(0)
                .fullShortUrl(fullShortUri)
                .build();

        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullShortUri)
                .gid(requestParam.getGid())
                .build();
        try{
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(shortLinkGotoDO);
        }catch (DuplicateKeyException ex){
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUri);
            ShortLinkDO hasShortLink = baseMapper.selectOne(queryWrapper);
            if(hasShortLink != null){
                throw new ServiceException(ShortLinkErrorCodeEnums.SHORT_LINK_INSERT_ERROR);
            }

        }
        // 缓存预热
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShortUri),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidDateTime(requestParam.getValidDate()),
                TimeUnit.MILLISECONDS
        );
        shortLinkUriCachePenetrationBloomFilter.add(fullShortUri);
        return ShortLinkCreateRespDTO
                .builder()
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .build();
    }

    /**
     * 短链接分页查询
     * @param requestParam 短链接分页查询参数
     * @return 短链接分页查询返回结果
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> result = baseMapper.selectPage(requestParam, queryWrapper);
        return result.convert((each) ->
                {
                    ShortLinkPageRespDTO resultPage = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
                    resultPage.setDomain("http://" + resultPage.getDomain());
                    return resultPage;
                }
        );
    }

    /**
     *
     * @param requestParam 分组标识
     * @return ShortLinkGroupRespDTO
     */
    @Override
    public List<ShortLinkGroupRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = new QueryWrapper<>();
                queryWrapper.select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList, ShortLinkGroupRespDTO.class);
    }

    /**
     * 修改短链接
     * @param requestParam 修改短链接请求参数
     * @return void
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);

        ShortLinkDO hasShortLink = baseMapper.selectOne(queryWrapper);

        if (hasShortLink == null){
            throw new ClientException("短链接记录不存在");
        }

        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(hasShortLink.getDomain())
                .shortUri(hasShortLink.getShortUri())
                .clickNum(hasShortLink.getClickNum())
                .favicon(hasShortLink.getFavicon())
                .createdType(hasShortLink.getCreatedType())
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .describe(requestParam.getDescribe())
                .validDate(requestParam.getValidDate())
                .validDateType(requestParam.getValidDateType())
                .build();

        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .set(Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()),
                        ShortLinkDO::getValidDate, null);
        if (!Objects.equals(hasShortLink.getGid(), requestParam.getGid())) {
            baseMapper.delete(updateWrapper);
        }
        baseMapper.update(shortLinkDO, updateWrapper);
    }

    /**
     * 短链接跳转
     * @param shortUri 获取跳转的短链接
     * @param request 请求
     * @param response 响应
     */
    @Override
    public void restoreUri(String shortUri, ServletRequest request, ServletResponse response) throws IOException {
        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUri;

        String originalUrl = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if(StringUtil.isNotBlank(originalUrl)){
            ((HttpServletResponse) response).sendRedirect(originalUrl);
            return;
        }

        // 判断布隆过滤器是否存在
        boolean contains = shortLinkUriCachePenetrationBloomFilter.contains(fullShortUrl);
        if(!contains){
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }

        // 短链接跳转是空值
        String goToIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_IS_NULL_KEY, fullShortUrl));
        if(StringUtil.isNotBlank(goToIsNullShortLink)){
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }

        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            //双检加锁策略，防止缓存击穿
            originalUrl = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if(StringUtil.isNotBlank(originalUrl)){
                ((HttpServletResponse) response).sendRedirect(originalUrl);
                return;
            }
            // 通过完整短链接去数据库找到对应的原始短链接
            LambdaQueryWrapper<ShortLinkGotoDO> linkGotoDOWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoDOWrapper);
            if(shortLinkGotoDO == null){
                stringRedisTemplate.opsForValue()
                        .set(String.format(GOTO_SHORT_LINK_IS_NULL_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            // 到link表查找原始短链接
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid());
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if(shortLinkDO == null || shortLinkDO.getValidDate().before(new Date())) {
                // 判断当前短链接是否已经过期
                stringRedisTemplate.opsForValue()
                        .set(String.format(GOTO_SHORT_LINK_IS_NULL_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidDateTime(shortLinkDO.getValidDate()),
                    TimeUnit.MILLISECONDS
            );
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
        }finally {
            lock.unlock();
        }
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

    /**
     * 获取短链接原始图标
     * @param url
     * @return
     */
    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }
}
