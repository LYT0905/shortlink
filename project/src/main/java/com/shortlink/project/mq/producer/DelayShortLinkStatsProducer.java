package com.shortlink.project.mq.producer;

import cn.hutool.core.lang.UUID;
import com.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.shortlink.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATS_KEY;

/**
 * 延迟消费短链接统计发送者
 * TODO 后续重构为RabbitMQ
 */
@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsProducer {

    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送延迟消费短链接统计
     *
     * @param statsRecord 短链接统计实体参数
     */
    public void send(ShortLinkStatsRecordDTO statsRecord) {
        statsRecord.setKeys(UUID.fastUUID().toString());
        RBlockingDeque<ShortLinkStatsRecordDTO> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE_STATS_KEY);
        RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        delayedQueue.offer(statsRecord, 5, TimeUnit.SECONDS);
    }

//    public void send(ShortLinkStatsRecordDTO statsRecord, long delayInSeconds) {
//        // 设置唯一标识符
//        statsRecord.setKeys(UUID.randomUUID().toString());
//
//        // 创建带有TTL属性的消息
//        MessageProperties properties = new MessageProperties();
//        properties.setExpiration(String.valueOf(delayInSeconds * 1000)); // 转换为毫秒
//
//        // 将实体转换为消息并设置属性
//        Message message = rabbitTemplate.getMessageConverter().toMessage(statsRecord, properties);
//
//        // 发送到延迟队列（这里假设delayExchangeName是配置好的延迟交换器名称）
//        rabbitTemplate.send("delayExchangeName", "routingKeyForDelayQueue", message);
//    }
}