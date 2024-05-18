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
 * @date 2024/03/09/14:56
 */

/**
 * 短链接监控之分页查询PV、UV、UIP实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_link_stats_today")
public class LinkStatsTodayDO extends BaseDO {
    /**
     * id
     */
    private Long id;
    /**
     * 短链接
     */
    private String fullShortUrl;

    /**
     * 日期
     */
    private Date date;

    /**
     * 今日pv
     */
    private Integer todayPv;

    /**
     * 今日uv
     */
    private Integer todayUv;

    /**
     * 今日ip数
     */
    private Integer todayUip;
}
