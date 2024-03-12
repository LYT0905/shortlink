package com.shortlink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
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
import com.shortlink.config.GotoDomainWhiteListConfiguration;
import com.shortlink.dao.entity.*;
import com.shortlink.dao.mapper.*;
import com.shortlink.dto.biz.ShortLinkStatsRecordDTO;
import com.shortlink.dto.request.ShortLinkBatchCreateReqDTO;
import com.shortlink.dto.request.ShortLinkCreateReqDTO;
import com.shortlink.dto.request.ShortLinkPageReqDTO;
import com.shortlink.dto.request.ShortLinkUpdateReqDTO;
import com.shortlink.dto.response.*;
import com.shortlink.mq.producer.DelayShortLinkStatsProducer;
import com.shortlink.service.LinkStatsTodayService;
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
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private final LinkStatsTodayService linkStatsTodayService;
    private final DelayShortLinkStatsProducer delayShortLinkStatsProducer;
    private final GotoDomainWhiteListConfiguration gotoDomainWhiteListConfiguration;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    /**
     * 创建短链接的同时插入到goto表方便定位查找
     * @param requestParam 短链接创建参数
     * @return ShortLinkCreateRespDTO
     */
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        verificationWhitelist(requestParam.getOriginUrl());
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain)
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(createShortLinkDefaultDomain)
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
                .fullShortUrl(fullShortUrl)
                .build();

        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .build();
        try{
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(shortLinkGotoDO);
        }catch (DuplicateKeyException ex){
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl);
            ShortLinkDO hasShortLink = baseMapper.selectOne(queryWrapper);
            if(hasShortLink != null){
                throw new ServiceException(ShortLinkErrorCodeEnums.SHORT_LINK_INSERT_ERROR);
            }

        }
        // 缓存预热
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidDateTime(requestParam.getValidDate()),
                TimeUnit.MILLISECONDS
        );
        shortLinkUriCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO
                .builder()
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .build();
    }

    /**
     * 批量创建短链接（批量其实就是批量插入原始链接和描述）
     * @param requestParam 请求创建参数
     * @return 返回参数
     */
    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        // 获取原始链接
        List<String> originUrls = requestParam.getOriginUrls();
        // 获取描述
        List<String> describes = requestParam.getDescribes();
        List<ShortLinkBaseInfoRespDTO> result = new ArrayList<>();
        for (int i = 0; i < originUrls.size(); i++){
            ShortLinkCreateReqDTO shortLinkCreateReqDTO = BeanUtil.toBean(requestParam, ShortLinkCreateReqDTO.class);
            shortLinkCreateReqDTO.setOriginUrl(originUrls.get(i));
            shortLinkCreateReqDTO.setDescribe(describes.get(i));
            try{
                ShortLinkCreateRespDTO shortLink = createShortLink(shortLinkCreateReqDTO);
                ShortLinkBaseInfoRespDTO shortLinkBaseInfoRespDTO = ShortLinkBaseInfoRespDTO.builder()
                        .fullShortUrl(shortLink.getFullShortUrl())
                        .originUrl(shortLink.getFullShortUrl())
                        .describe(describes.get(i))
                        .build();
                result.add(shortLinkBaseInfoRespDTO);
            } catch (Throwable ex) {
                log.error("批量创建短链接失败，原始参数：{}", originUrls.get(i));
            }
        }
        return ShortLinkBatchCreateRespDTO.builder()
                .total(result.size())
                .baseLinkInfos(result)
                .build();
    }

    /**
     * 短链接统计
     *
     * @param fullShortUrl         完整短链接
     * @param gid                  分组标识
     * @param statsRecord 短链接统计实体参数
     */
    @Override
    public void shortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord) {
        fullShortUrl = Optional.ofNullable(fullShortUrl).orElse(statsRecord.getFullShortUrl());
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, fullShortUrl));
        RLock rLock = readWriteLock.readLock();
        if (!rLock.tryLock()) {
            delayShortLinkStatsProducer.send(statsRecord);
            return;
        }
        try {
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
                gid = shortLinkGotoDO.getGid();
            }
            int hour = DateUtil.hour(new Date(), true);
            Week week = DateUtil.dayOfWeekEnum(new Date());
            int weekValue = week.getIso8601Value();
            // 统计访问数据
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(statsRecord.getUvFirstFlag() ? 1 : 0)
                    .uip(statsRecord.getUipFirstFlag() ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
            // 调用地图接口
            Map<String, Object> localeParamMap = new HashMap<>();
            localeParamMap.put("key", statsLocaleAmapKey);
            localeParamMap.put("ip", statsRecord.getRemoteAddr());
            String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
            JSONObject localeResultObj = JSON.parseObject(localeResultStr);
            String infoCode = localeResultObj.getString("infocode");
            String actualProvince = "未知";
            String actualCity = "未知";
            if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
                String province = localeResultObj.getString("province");
                boolean unknownFlag = StrUtil.equals(province, "[]");
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .province(actualProvince = unknownFlag ? actualProvince : province)
                        .city(actualCity = unknownFlag ? actualCity : localeResultObj.getString("city"))
                        .adcode(unknownFlag ? "未知" : localeResultObj.getString("adcode"))
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .country("中国")
                        .gid(gid)
                        .date(new Date())
                        .build();
                linkLocaleStatsMapper.shortLinkLocaleState(linkLocaleStatsDO);
            }
            Date date = new Date();
            // 操作系统数据统计
            LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                    .os(statsRecord.getOs())
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(date)
                    .build();
            linkOsStatsMapper.shortLinkOsState(linkOsStatsDO);
            // 浏览器访问数据统计
            LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                    .browser(statsRecord.getBrowser())
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(date)
                    .build();
            linkBrowserStatsMapper.shortLinkBrowserState(linkBrowserStatsDO);
            // 设备类型访问数据统计
            LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                    .device(statsRecord.getDevice())
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(date)
                    .build();
            linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);
            // 网络类型数据统计
            LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                    .network(statsRecord.getNetwork())
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(date)
                    .build();
            linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);
            // 监控日志数据统计
            LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                    .user(statsRecord.getUv())
                    .ip(statsRecord.getRemoteAddr())
                    .browser(statsRecord.getBrowser())
                    .os(statsRecord.getOs())
                    .network(statsRecord.getNetwork())
                    .device(statsRecord.getDevice())
                    .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .build();
            linkAccessLogsMapper.insert(linkAccessLogsDO);
            // 短链接访问自增
            baseMapper.incrementStats(gid, fullShortUrl, 1, statsRecord.getUvFirstFlag() ? 1 : 0, statsRecord.getUipFirstFlag() ? 1 : 0);

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
                    DateUtil.formatDate(date) + ":" + fullShortUrl, statsRecord.getUv());
            boolean todayUvFlag = todayUvAdded != null && todayUvAdded > 0L;
            // 设置一天的过期时间
            stringRedisTemplate.expire(TODAY_SHORT_LINK_UV +
                    DateUtil.formatDate(date) + ":" + fullShortUrl, timestamp, TimeUnit.MILLISECONDS);

            Long todayUIpAdded = stringRedisTemplate.opsForSet().add(TODAY_SHORT_LINK_UIP + DateUtil.formatDate(date) + ":"
                    + fullShortUrl, statsRecord.getRemoteAddr());
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
        } catch (Throwable ex) {
            log.error("短链接访问量统计异常", ex);
        } finally {
            rLock.unlock();
        }
    }

    /**
     * 短链接分页查询
     * @param requestParam 短链接分页查询参数
     * @return 短链接分页查询返回结果
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        IPage<ShortLinkDO> resultPage = baseMapper.pageLink(requestParam);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
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
                .eq("del_flag", 0)
                .eq("del_time", 0)
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
        verificationWhitelist(requestParam.getOriginUrl());
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);

        ShortLinkDO hasShortLink = baseMapper.selectOne(queryWrapper);

        if (hasShortLink == null){
            throw new ClientException("短链接记录不存在");
        }

        // 判断是否修改同一分组下的短链接，如果不是则要删除原来的短链接，因为分表是根据gid分的，如果不删除，那么将找不到数据，反之更新
        if (Objects.equals(hasShortLink.getGid(), requestParam.getGid())) {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .set(Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()),
                            ShortLinkDO::getValidDate, null);

            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .domain(hasShortLink.getDomain())
                    .shortUri(hasShortLink.getShortUri())
                    .favicon(hasShortLink.getFavicon())
                    .createdType(hasShortLink.getCreatedType())
                    .gid(requestParam.getGid())
                    .originUrl(requestParam.getOriginUrl())
                    .describe(requestParam.getDescribe())
                    .validDate(requestParam.getValidDate())
                    .validDateType(requestParam.getValidDateType())
                    .build();

            baseMapper.update(shortLinkDO, updateWrapper);
        }else {
            // 读写锁(读锁是共享锁，写锁是独占)，保证在更改数据时，没有人可以抢到锁，从而保证正在修改的短链接无法被访问
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock
                    (String.format(LOCK_GID_UPDATE_KEY, requestParam.getFullShortUrl()));
            RLock rLock = readWriteLock.writeLock();
            // 没有获取到写锁
            if (!rLock.tryLock()){
                throw new ServiceException("短链接正在被访问，请稍后再试");
            }
            try {
                LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                        .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkDO::getGid, hasShortLink.getGid())
                        .eq(ShortLinkDO::getDelFlag, 0)
                        .eq(ShortLinkDO::getDelTime, 0L)
                        .eq(ShortLinkDO::getEnableStatus, 0);
                ShortLinkDO delShortLinkDO = ShortLinkDO.builder()
                        .delTime(System.currentTimeMillis())
                        .build();
                // 删除原来的
                delShortLinkDO.setDelFlag(1);
                baseMapper.update(delShortLinkDO, updateWrapper);
                // 插入修改的
                ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                        .domain(createShortLinkDefaultDomain)
                        .originUrl(requestParam.getOriginUrl())
                        .gid(requestParam.getGid())
                        .createdType(hasShortLink.getCreatedType())
                        .validDateType(requestParam.getValidDateType())
                        .validDate(requestParam.getValidDate())
                        .describe(requestParam.getDescribe())
                        .shortUri(hasShortLink.getShortUri())
                        .enableStatus(hasShortLink.getEnableStatus())
                        .totalPv(hasShortLink.getTotalPv())
                        .totalUv(hasShortLink.getTotalUv())
                        .totalUip(hasShortLink.getTotalUip())
                        .fullShortUrl(hasShortLink.getFullShortUrl())
                        .favicon(getFavicon(requestParam.getOriginUrl()))
                        .delTime(0L)
                        .build();
                baseMapper.insert(shortLinkDO);

                LambdaQueryWrapper<LinkStatsTodayDO> statsTodayQueryWrapper = Wrappers.lambdaQuery(LinkStatsTodayDO.class)
                        .eq(LinkStatsTodayDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkStatsTodayDO::getGid, hasShortLink.getGid())
                        .eq(LinkStatsTodayDO::getDelFlag, 0);
                List<LinkStatsTodayDO> linkStatsTodayDOList = linkStatsTodayMapper.selectList(statsTodayQueryWrapper);
                if (CollUtil.isNotEmpty(linkStatsTodayDOList)){
                    // 修改统计每天记录数据的gid(先删后加)
                    linkStatsTodayMapper.deleteBatchIds(linkStatsTodayDOList.stream()
                            .map(LinkStatsTodayDO::getId)
                            .toList()
                    );
                    linkStatsTodayDOList.forEach(each -> each.setGid(requestParam.getGid()));
                    linkStatsTodayService.saveBatch(linkStatsTodayDOList);
                }
                // 一样修跳转表的gid（先删后加）
                LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkGotoDO::getGid, hasShortLink.getGid());
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
                shortLinkGotoMapper.deleteById(shortLinkGotoDO.getId());
                shortLinkGotoDO.setGid(requestParam.getGid());
                shortLinkGotoMapper.insert(shortLinkGotoDO);
                // 直接改基础统计数据表的gid，因为该表没有进行分表，也不是通过gid进行分表
                LambdaUpdateWrapper<LinkAccessStatsDO> linkAccessStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessStatsDO.class)
                        .eq(LinkAccessStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessStatsDO::getGid, hasShortLink.getGid())
                        .eq(LinkAccessStatsDO::getDelFlag, 0);
                LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkAccessStatsMapper.update(linkAccessStatsDO, linkAccessStatsUpdateWrapper);
                // 修改地区数据表的gid，不需要删，原因同上
                LambdaUpdateWrapper<LinkLocaleStatsDO> linkLocaleStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkLocaleStatsDO.class)
                        .eq(LinkLocaleStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkLocaleStatsDO::getGid, hasShortLink.getGid())
                        .eq(LinkLocaleStatsDO::getDelFlag, 0);
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkLocaleStatsMapper.update(linkLocaleStatsDO, linkLocaleStatsUpdateWrapper);
                // 修改操作系统数据表的gid，不需要删，原因同上
                LambdaUpdateWrapper<LinkOsStatsDO> linkOsStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkOsStatsDO.class)
                        .eq(LinkOsStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkOsStatsDO::getGid, hasShortLink.getGid())
                        .eq(LinkOsStatsDO::getDelFlag, 0);
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkOsStatsMapper.update(linkOsStatsDO, linkOsStatsUpdateWrapper);
                // 修改浏览器数据表的gid，不需要删，原因同上
                LambdaUpdateWrapper<LinkBrowserStatsDO> linkBrowserStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkBrowserStatsDO.class)
                        .eq(LinkBrowserStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkBrowserStatsDO::getGid, hasShortLink.getGid())
                        .eq(LinkBrowserStatsDO::getDelFlag, 0);
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkBrowserStatsMapper.update(linkBrowserStatsDO, linkBrowserStatsUpdateWrapper);
                // 修改设备数据表的gid，不需要删，原因同上
                LambdaUpdateWrapper<LinkDeviceStatsDO> linkDeviceStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkDeviceStatsDO.class)
                        .eq(LinkDeviceStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkDeviceStatsDO::getGid, hasShortLink.getGid())
                        .eq(LinkDeviceStatsDO::getDelFlag, 0);
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkDeviceStatsMapper.update(linkDeviceStatsDO, linkDeviceStatsUpdateWrapper);
                // 修改网络数据表的gid，不需要删，原因同上
                LambdaUpdateWrapper<LinkNetworkStatsDO> linkNetworkStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkNetworkStatsDO.class)
                        .eq(LinkNetworkStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkNetworkStatsDO::getGid, hasShortLink.getGid())
                        .eq(LinkNetworkStatsDO::getDelFlag, 0);
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkNetworkStatsMapper.update(linkNetworkStatsDO, linkNetworkStatsUpdateWrapper);
                // 修改监控日志数据表的gid，不需要删，原因同上
                LambdaUpdateWrapper<LinkAccessLogsDO> linkAccessLogsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessLogsDO.class)
                        .eq(LinkAccessLogsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessLogsDO::getGid, hasShortLink.getGid())
                        .eq(LinkAccessLogsDO::getDelFlag, 0);
                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkAccessLogsMapper.update(linkAccessLogsDO, linkAccessLogsUpdateWrapper);
            } finally {
                rLock.unlock();
            }
        }
        // 判断过了有效期
        if (!Objects.equals(hasShortLink.getValidDateType(), requestParam.getValidDateType())
                || !Objects.equals(hasShortLink.getValidDate(), requestParam.getValidDate())) {
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
            // 如果是有有效期，并且已经过期了，缓存中才有is_null的key
            if (hasShortLink.getValidDate() != null && hasShortLink.getValidDate().before(new Date())){
                // 如果修改为永久有效或者是合法有效期
                if (Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()) ||
                        requestParam.getValidDate().after(new Date())){
                    stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_IS_NULL_KEY, requestParam.getFullShortUrl()));
                }
            }
        }
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

        String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each, "80"))
                .map(String::valueOf)
                .map(each -> ":" + each)
                .orElse("");

        String fullShortUrl = serverName + serverPort + "/" + shortUri;

        String originalUrl = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if(StringUtil.isNotBlank(originalUrl)){
            ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            shortLinkStats(fullShortUrl, null, statsRecord);
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
                ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
                shortLinkStats(fullShortUrl, null, statsRecord);
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
            ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            shortLinkStats(fullShortUrl, null, statsRecord);
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
            if (!shortLinkUriCachePenetrationBloomFilter.contains(createShortLinkDefaultDomain + "/" + shorUri)) {
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

    /**
     * 将之前统计短链接数据的方法进行了抽取，实现方法复用
     * @param fullShortUrl
     * @param request
     * @param response
     * @return
     */
    private ShortLinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String fullShortUrl, ServletRequest request, ServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        AtomicReference<String> uv = new AtomicReference<>();
        Runnable addResponseCookieTask = () -> {
            uv.set(UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse) response).addCookie(uvCookie);
            uvFirstFlag.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uv.get());
        };
        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies)
                    .filter(each -> Objects.equals(each.getName(), "uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        uv.set(each);
                        Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, each);
                        uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                    }, addResponseCookieTask);
        } else {
            addResponseCookieTask.run();
        }
        String remoteAddr = LinkUtil.getActualIp(((HttpServletRequest) request));
        String os = LinkUtil.getOs(((HttpServletRequest) request));
        String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
        String device = LinkUtil.getDevice(((HttpServletRequest) request));
        String network = LinkUtil.getNetwork(((HttpServletRequest) request));
        Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, remoteAddr);
        boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
        return ShortLinkStatsRecordDTO.builder()
                .fullShortUrl(fullShortUrl)
                .uv(uv.get())
                .uvFirstFlag(uvFirstFlag.get())
                .uipFirstFlag(uipFirstFlag)
                .remoteAddr(remoteAddr)
                .os(os)
                .browser(browser)
                .device(device)
                .network(network)
                .build();
    }

    /**
     * 判断是否是白名单链接
     * @param originUrl
     */
    private void verificationWhitelist(String originUrl) {
        Boolean enable = gotoDomainWhiteListConfiguration.getEnable();
        if (enable == null || !enable) {
            return;
        }
        String domain = LinkUtil.extractDomain(originUrl);
        if (StrUtil.isBlank(domain)) {
            throw new ClientException("跳转链接填写错误");
        }
        List<String> details = gotoDomainWhiteListConfiguration.getDetails();
        if (!details.contains(domain)) {
            throw new ClientException("演示环境为避免恶意攻击，请生成以下网站跳转链接：" + gotoDomainWhiteListConfiguration.getNames());
        }
    }

    /**
     * 短链接基础数据统计
     * @param fullShortUrl 完整短链接
     * @param gid 分组标识
     * @param request 请求
     * @param response 响应
     */
    /**
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
    **/
}
