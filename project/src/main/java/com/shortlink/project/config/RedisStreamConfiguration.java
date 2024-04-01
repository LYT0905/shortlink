package com.shortlink.project.config;

import com.shortlink.project.mq.consumer.ShortLinkStatsSaveConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @author LYT0905
 * @date 2024/03/13/12:50
 */

/**
 * Redis Stream 消息队列配置
 */
@Configuration
@RequiredArgsConstructor
public class RedisStreamConfiguration {
    private final RedisConnectionFactory redisConnectionFactory;
    private final ShortLinkStatsSaveConsumer shortLinkStatsSaveConsumer;



//    @Bean
//    public ExecutorService asyncStreamConsumer() {
//        AtomicInteger index = new AtomicInteger();
//        int processors = Runtime.getRuntime().availableProcessors();
//        return new ThreadPoolExecutor(processors,
//                processors + processors >> 1,
//                60,
//                TimeUnit.SECONDS,
//                new LinkedBlockingQueue<>(),
//                runnable -> {
//                    Thread thread = new Thread(runnable);
//                    thread.setName("stream_consumer_short-link_stats_" + index.incrementAndGet());
//                    thread.setDaemon(true);
//                    return thread;
//                }
//        );
//    }
//
//    @Bean(initMethod = "start", destroyMethod = "stop")
//    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(ExecutorService asyncStreamConsumer) {
//        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
//                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
//                        .builder()
//                        // 一次最多获取多少条消息
//                        .batchSize(10)
//                        // 执行从 Stream 拉取到消息的任务流程
//                        .executor(asyncStreamConsumer)
//                        // 如果没有拉取到消息，需要阻塞的时间。不能大于 ${spring.data.redis.timeout}，否则会超时
//                        .pollTimeout(Duration.ofSeconds(3))
//                        .build();
//        StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer =
//                StreamMessageListenerContainer.create(redisConnectionFactory, options);
//        streamMessageListenerContainer.receiveAutoAck(Consumer.from(SHORT_LINK_STATS_STREAM_GROUP_KEY, "stats-consumer"),
//                StreamOffset.create(SHORT_LINK_STATS_STREAM_TOPIC_KEY, ReadOffset.lastConsumed()), shortLinkStatsSaveConsumer);
//        return streamMessageListenerContainer;
//    }
}
