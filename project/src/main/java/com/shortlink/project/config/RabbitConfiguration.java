package com.shortlink.project.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LYT0905
 * @date 2024/03/15/17:19
 */

@Configuration
public class RabbitConfiguration{
    private final static String statsSaveExchange = "short_link_stats_exchange";
    private final static String statsSaveRoutingKey = "short_link_stats_routing_key";
    private final static String queue = "short-link-queue";
    @Bean
    public Queue delayedQueue(){
        return new Queue(queue);
    }

    @Bean
    public CustomExchange delayedExchange(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-delayed-type", "direct");
        return new CustomExchange(statsSaveExchange, "x-delayed-message",
                true, false, arguments);
    }

    @Bean
    public Binding bindingDelayedQueue(@Qualifier("delayedQueue") Queue queue,
                                       @Qualifier("delayedExchange") CustomExchange customExchange){
        return BindingBuilder.bind(queue).to(customExchange).with(statsSaveRoutingKey).noargs();
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 设置重试策略，这里简单设置重试3次
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(3); // 调整此值以设置最大重试次数
        retryTemplate.setRetryPolicy(simpleRetryPolicy);

        // 设置背压策略，这里使用固定的间隔时间
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(5000L); // 设置每次重试之间的间隔，单位是毫秒
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

}
