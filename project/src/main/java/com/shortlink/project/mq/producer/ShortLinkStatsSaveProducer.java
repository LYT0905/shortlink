package com.shortlink.project.mq.producer;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 短链接监控状态保存消息队列生产者
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShortLinkStatsSaveProducer {
    private final RabbitTemplate rabbitTemplate;
    private final RetryTemplate retryTemplate;

    private final static String statsSaveExchange = "short_link_stats_exchange";
    private final static String statsSaveRoutingKey = "short_link_stats_routing_key";



    /**
     * 发送延迟消费短链接统计
     */
//    public void send(Map<String, String> producerMap) {
//        stringRedisTemplate.opsForStream().add(SHORT_LINK_STATS_STREAM_TOPIC_KEY, producerMap);
//    }

    /**
     * 发送延迟消费短链接统计
     */
    public void send(Map<String, String> producerMap) {
        String keys = UUID.randomUUID().toString();
        producerMap.put("keys", keys);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentEncoding("UTF-8");
        retryTemplate.execute(retryContext -> {
            try {
                rabbitTemplate.convertAndSend(statsSaveExchange, statsSaveRoutingKey, producerMap);
                log.info("[消息访问统计监控] 消息发送成功，消息Keys: {}", keys);
            }catch (Throwable ex){
                log.error("[消息访问统计监控] 消息发送失败，消息体: {}", JSON.toJSONString(producerMap), ex);
                // 自定义错误处理行为...
            }
            return true;
        });

    }
}