package com.shortlink.project.mq.consumer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shortlink.project.common.convention.exception.ServiceException;
import com.shortlink.project.dao.entity.*;
import com.shortlink.project.dao.mapper.*;
import com.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.shortlink.project.mq.idempotent.MessageQueueIdempotentHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.shortlink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;

/**
 * 短链接监控状态保存消息队列消费者
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShortLinkStatsSaveConsumer{

    private final ShortLinkMapper shortLinkMapper;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

    @RabbitListener(queues = "short-link-queue")
    public void onMessage(Map<String, String> producerMap){
        String keys = producerMap.get("keys");
        // 判断是否被消费过
        if (!messageQueueIdempotentHandler.isMessageProcessed(keys)){
            // 判断消费流程是否执行完（避免出现因为一些极端原因（如断电），导致实际上没有消费但是判断消费的情况）
            if (messageQueueIdempotentHandler.isAccomplish(keys)){
                return;
            }
            throw new ServiceException("消息未完成流程，需要消息队列进行重试");
        }
        // 使用throwable是怕出现有的异常无法通过exception进行捕捉
        try {
            String fullShortUrl = producerMap.get("fullShortUrl");
            if (StrUtil.isNotBlank(fullShortUrl)){
                String gid = producerMap.get("gid");
                ShortLinkStatsRecordDTO statsRecordDTO = JSON.parseObject(producerMap.get("statsRecord"), ShortLinkStatsRecordDTO.class);
                actualSaveShortLinkStats(fullShortUrl, gid, statsRecordDTO);
            }
        }catch (Throwable throwable){
            // 某某某情况宕机了
            try{
                messageQueueIdempotentHandler.delMessageProcessed(keys);
            }catch (Throwable ex){
                log.error("删除幂等标识错误", ex);
            }
            throw throwable;
        }
        // 设置消息流程执行完成
        messageQueueIdempotentHandler.setAccomplish(keys);
    }

    /**
     * 当接收到消息时的处理逻辑。
     *
     * @param message 包含消息详细信息的对象，包括消息流(stream)、消息ID(id)以及消息内容(value)。
     *                消息内容以Map形式存储，包含全短链(fullShortUrl)、组ID(gid)和统计记录(statsRecord)等信息。
     */
//    @Override
//    public void onMessage(MapRecord<String, String, String> message) {
//        String stream = message.getStream();
//        RecordId id = message.getId();
//
//        // 判断是否被消费过
//        if (!messageQueueIdempotentHandler.isMessageProcessed(id.toString())){
//            // 判断消费流程是否执行完（避免出现因为一些极端原因（如断电），导致实际上没有消费但是判断消费的情况）
//            if (messageQueueIdempotentHandler.isAccomplish(id.toString())){
//                return;
//            }
//            throw new ServiceException("消息未完成流程，需要消息队列进行重试");
//        }
//        // 使用throwable是怕出现有的异常无法通过exception进行捕捉
//        try {
//            Map<String, String> producerMap = message.getValue();
//            String fullShortUrl = producerMap.get("fullShortUrl");
//            if (StrUtil.isNotBlank(fullShortUrl)) {
//                String gid = producerMap.get("gid");
//                ShortLinkStatsRecordDTO statsRecord = JSON.parseObject(producerMap.get("statsRecord"), ShortLinkStatsRecordDTO.class);
//                actualSaveShortLinkStats(fullShortUrl, gid, statsRecord);
//            }
//            stringRedisTemplate.opsForStream().delete(Objects.requireNonNull(stream), id.getValue());
//        }catch (Throwable throwable){
//            // 某某某情况宕机了
//            messageQueueIdempotentHandler.delMessageProcessed(id.toString());
//        }
//        // 设置消息流程执行完成
//        messageQueueIdempotentHandler.setAccomplish(id.toString());
//    }

    /**
     * 统计基础访问数据
     * @param fullShortUrl 完整短链接
     * @param gid 分组标识
     * @param statsRecord 数据集合
     */
    public void actualSaveShortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord) {
        Date date = new Date();
        fullShortUrl = Optional.ofNullable(fullShortUrl).orElse(statsRecord.getFullShortUrl());
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, fullShortUrl));
        RLock rLock = readWriteLock.readLock();
        rLock.lock();
        try {
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
                gid = shortLinkGotoDO.getGid();
            }
            int hour = DateUtil.hour(date, true);
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
                    .date(date)
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
                        .date(date)
                        .build();
                linkLocaleStatsMapper.shortLinkLocaleState(linkLocaleStatsDO);
            }
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
            shortLinkMapper.incrementStats(gid, fullShortUrl, 1, statsRecord.getUvFirstFlag() ? 1 : 0, statsRecord.getUipFirstFlag() ? 1 : 0);

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
}