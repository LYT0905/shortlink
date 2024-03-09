package com.shortlink.dao.entity;

/**
 * @author LYT0905
 * @date 2024/03/08/15:13
 */

import com.baomidou.mybatisplus.annotation.TableName;
import com.shortlink.common.datatbase.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 访问日志监控实体
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_link_access_logs")
public class LinkAccessLogsDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 用户信息
     */
    private String user;

    /**
     * ip
     */
    private String ip;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 访问网络
     */
    private String network;

    /**
     * 访问设备
     */
    private String device;

    /**
     * 地区
     */
    private String locale;

}
