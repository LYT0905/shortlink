package com.shortlink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
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
import com.shortlink.dao.entity.*;
import com.shortlink.dao.mapper.*;
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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.shortlink.common.constant.RedisKeyConstant.*;
import static com.shortlink.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;

/**
 * @author LYT0905
 * @date 2024/03/03/21:42
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortLinkUriCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

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
                .totalPv(0)
                .totalUip(0)
                .totalUv(0)
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
            shortLinkStats(fullShortUrl, null, request, response);
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
                shortLinkStats(fullShortUrl, null, request, response);
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
            // 判断当前短链接是否存在数据库或者已经过期
            if(shortLinkDO == null || (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date()))) {
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
            shortLinkStats(fullShortUrl, shortLinkDO.getGid(), request, response);
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
        }finally {
            lock.unlock();
        }
    }

    /**
     * 短链接基础数据统计
     * @param fullShortUrl 完整短链接
     * @param gid 分组标识
     * @param request 请求
     * @param response 响应
     */
    public void shortLinkStats(String fullShortUrl, String gid, ServletRequest request, ServletResponse response){
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        // 设置cookies
        try{
            AtomicReference<String> uv = new AtomicReference<>();
            Runnable addCookiesForResponse = () -> {
                uv.set(UUID.fastUUID().toString());
                Cookie uvCookie = new Cookie("uv", uv.get());
                uvCookie.setMaxAge(60 * 60 * 24 * 30);
                uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.lastIndexOf("/"), fullShortUrl.length()));
                ((HttpServletResponse) response).addCookie(uvCookie);
                uvFirstFlag.set(Boolean.TRUE);
                stringRedisTemplate.opsForSet().add("short-link_stats-uv:" + fullShortUrl, uv.get());
            };
            if(ArrayUtil.isNotEmpty(cookies)){
                Arrays.stream(cookies)
                        .filter((each) -> Objects.equals(each.getName(), "uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse((each) -> {
                            uv.set(each);
                            Long uvAdded = stringRedisTemplate.opsForSet().add("short-link_stats-uv:" + fullShortUrl, each);
                            uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                        }, addCookiesForResponse);
            }else {
                addCookiesForResponse.run();
            }
            // 短链接基础数据统计
            String actualIp = LinkUtil.getActualIp((HttpServletRequest)request);
            Long uipAdded = stringRedisTemplate.opsForSet().add("short-link-stats-uip:" + fullShortUrl, actualIp);
            boolean uipFirstFlag = (uipAdded != null && uipAdded > 0L);

            if(StringUtil.isBlank(gid)){
                LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
                gid = shortLinkGotoDO.getGid();
            }
            // 防止出现时间错误
            Date date = new Date();

            int hour = DateUtil.hour(date, true);
            Week week = DateUtil.dayOfWeekEnum(date);
            int weekValue = week.getIso8601Value();

            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFirstFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(date)
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);

            // 调取高德地图接口(短链接地区数据统计)
            Map<String, Object> map = new HashMap<>();
            map.put("key", statsLocaleAmapKey);
            map.put("ip", actualIp);
            String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, map);
            JSONObject localeResultObject = JSON.parseObject(localeResultStr);
            String  infocode = localeResultObject.getString("infocode");
            // 插入地区统计数据
            String actualProvince;
            String actualCity;
            if(StringUtil.isNotBlank(infocode) && Objects.equals(infocode, "10000")){
                String province = localeResultObject.getString("province");
                boolean unknownFlag = StringUtil.equals(province, "[]");
                LinkLocaleStatsDO linkLocaleStats = LinkLocaleStatsDO.builder()
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .date(date)
                        .country("中国")
                        .province(actualProvince = unknownFlag ? "未知" : province)
                        .cnt(1)
                        .adcode(unknownFlag ? "未知" : localeResultObject.getString("adcode"))
                        .city(actualCity = unknownFlag ? "未知" : localeResultObject.getString("city"))
                        .build();
                linkLocaleStatsMapper.shortLinkLocaleState(linkLocaleStats);

                // 插入操作系统消息
                String os = LinkUtil.getOs((HttpServletRequest) request);
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .cnt(1)
                        .os(os)
                        .date(date)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .build();
                linkOsStatsMapper.shortLinkOsState(linkOsStatsDO);

                // 插入浏览器信息数据
                String browser = LinkUtil.getBrowser((HttpServletRequest) request);
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(date)
                        .browser(browser)
                        .build();
                linkBrowserStatsMapper.shortLinkBrowserState(linkBrowserStatsDO);

                // 插入访问设备类型
                String device = LinkUtil.getDevice((HttpServletRequest) request);
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .cnt(1)
                        .date(date)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .device(device)
                        .build();
                linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);

                // 插入网络类型，监控网络
                String network = LinkUtil.getNetwork((HttpServletRequest) request);
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .network(network)
                        .cnt(1)
                        .date(date)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .build();
                linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);

                // 访问日志监控（统计高频IP可以使用）
                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                        .ip(actualIp)
                        .browser(browser)
                        .device(device)
                        .network(network)
                        .locale(String.join("-", "中国", actualProvince + actualCity))
                        .os(os)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .user(uv.get())
                        .build();
                linkAccessLogsMapper.insert(linkAccessLogsDO);

                // 短链接访问统计自增
                baseMapper.incrementStats(gid, fullShortUrl, 1,
                        uvFirstFlag.get() ? 1 : 0, uipFirstFlag ? 1 : 0);

                // 获取当前日期
                LocalDateTime currentDateTime = LocalDateTime.now();
                // 设定时间为当天的23:59:59
                LocalDateTime endOfDay = currentDateTime.with(LocalTime.MAX);
                // 转换为ZonedDateTime以获取时区信息
                ZonedDateTime zonedDateTime = endOfDay.atZone(ZoneId.systemDefault());
                // 转换为时间戳（毫秒）
                long timestamp = zonedDateTime.toInstant().toEpochMilli();

                // 记录今日统计监控数据
                Long todayUvAdded = stringRedisTemplate.opsForSet().add(TODAY_SHORT_LINK_UV +
                        DateUtil.formatDate(date) + ":" + fullShortUrl, uv.get());
                boolean todayUvFlag = todayUvAdded != null && todayUvAdded > 0L;
                // 设置一天的过期时间
                stringRedisTemplate.expire(TODAY_SHORT_LINK_UV +
                        DateUtil.formatDate(date) + ":" + fullShortUrl, timestamp, TimeUnit.MILLISECONDS);

                Long todayUIpAdded = stringRedisTemplate.opsForSet().add(TODAY_SHORT_LINK_UIP + DateUtil.formatDate(date) + ":"
                        + fullShortUrl, actualIp);
                boolean todayUIpFlag = todayUIpAdded != null && todayUIpAdded > 0L;
                // 设置一天的过期时间127
                stringRedisTemplate.expire(TODAY_SHORT_LINK_UIP + DateUtil.formatDate(date) + ":"
                        + fullShortUrl, timestamp, TimeUnit.MILLISECONDS);

                LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                        .todayPv(1)
                        .todayUv(todayUvFlag ? 1 : 0)
                        .todayUip(todayUIpFlag ? 1 : 0)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .date(date)
                        .build();
                linkStatsTodayMapper.shortLinkTodayState(linkStatsTodayDO);
            }
        }catch (Exception ex){
            log.info("短链接数据统计出错", ex);
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
