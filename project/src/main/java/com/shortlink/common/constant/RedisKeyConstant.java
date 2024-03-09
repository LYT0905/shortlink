package com.shortlink.common.constant;

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
    public final static String GOTO_SHORT_LINK_KEY = "short-link_goto_%s";

    /**
     * 短链接空值跳转前缀
     */
    public final static String GOTO_SHORT_LINK_IS_NULL_KEY = "short-link_is-null_goto_%s";

    /**
     * 短链接跳转锁前缀
     */
    public final static String LOCK_GOTO_SHORT_LINK_KEY = "short-link_lock_goto_%s";

    /**
     * 今日用户访问数据前缀
     */
    public final static String TODAY_SHORT_LINK_UV = "short-link:stats:uv:";

    /**
     * 今日用户访问ip数据前缀
     */
    public final static String TODAY_SHORT_LINK_UIP = "short-link:stats:uip:";
}
