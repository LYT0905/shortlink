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
 * @date 2024/03/08/13:26
 */

/**
 * 地区访问实体
 */
@Data
@TableName("t_link_locale_stats")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkLocaleStatsDO extends BaseDO {
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
     * 省份名称
     */
    private String province;

    /**
     * 市名称
     */
    private String city;

    /**
     * 城市编码
     */
    private String adcode;

    /**
     * 国家标识
     */
    private String country;

}
