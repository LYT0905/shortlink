package com.shortlink.common.constant;

/**
 * @author LYT0905
 * @date 2024/02/29/13:42
 */

/**
 * 短链接后台管理 Redis 缓存常量类
 */
public class RedisCacheConstant {
    /**
     * 用户注册分布式锁
     */
    public final static String LOCK_USER_REGISTER_KEY = "short-link:lock_user_register:";

    /**
     * 分组创建分布式锁
     */
    public static final String LOCK_GROUP_CREATE_KEY = "short-link:lock_group-create:%s";
}
