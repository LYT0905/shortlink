package com.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shortlink.project.common.datatbase.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author LYT0905
 * @date 2024/03/08/18:46
 */

/**
 * 访问网络统计实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_link_network_stats")
public class LinkNetworkStatsDO extends BaseDO {
    /**
     * id
     */
    private Long id;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 日期
     */
    private Date date;

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 访问网络
     */
    private String network;
}
