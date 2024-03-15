package com.shortlink.project.common.constant;

/**
 * @author LYT0905
 * @date 2024/02/29/13:42
 */

/**
 * Redis Key常量类
 */
public class RedisKeyConstant {

    /**
     * 短链接跳转前缀
     */
    public final static String GOTO_SHORT_LINK_KEY = "short-link:goto:%s";

    /**
     * 短链接空值跳转前缀
     */
    public final static String GOTO_SHORT_LINK_IS_NULL_KEY = "short-link:is-null:goto_%s";

    /**
     * 短链接跳转锁前缀
     */
    public final static String LOCK_GOTO_SHORT_LINK_KEY = "short-link:lock:goto:%s";

    /**
     * 短链接统计判断是否新用户缓存标识
     */
    public final static String TODAY_SHORT_LINK_UV = "short-link:stats:uv:";

    /**
     * 短链接统计判断是否新 IP 缓存标识
     */
    public final static String TODAY_SHORT_LINK_UIP = "short-link:stats:uip:";

    /**
     * 短链接修改分组 ID 锁前缀 Key
     */
    public static final String LOCK_GID_UPDATE_KEY = "short-link:lock:update-gid:%s";

    /**
     * 短链接延迟队列消费统计 Key
     */
    public static final String DELAY_QUEUE_STATS_KEY = "short-link:delay-queue:stats";

    /**
     * 短链接监控消息保存队列 Topic 缓存标识
     */
    public static final String SHORT_LINK_STATS_STREAM_TOPIC_KEY = "short-link:stats-stream";

    /**
     * 短链接监控消息保存队列 Group 缓存标识
     */
    public static final String SHORT_LINK_STATS_STREAM_GROUP_KEY = "short-link:stats-stream:only-group";
}
