package com.shortlink.project.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author LYT0905
 * @date 2024/03/04/18:01
 */

@Data
public class ShortLinkPageRespDTO {
    /**
     * id
     */
    private Long id;

    /**
     * 域名
     */
    private String domain;

    /**
     * 短链接
     */
    private String shortUri;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 有效期类型 0：永久有效 1：自定义
     */
    private int validDateType;

    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validDate;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 描述
     */
    @TableField("`describe`")
    private String describe;

    /**
     * 网络图标
     */
    private String favicon;

    /**
     * 历史Pv
     */
    private String totalPv;

    /**
     * 今日Pv
     */
    private String todayPv;

    /**
     * 历史Uv
     */
    private String totalUv;

    /**
     * 今日Uv
     */
    private String todayUv;

    /**
     * 历史UIp
     */
    private String totalUip;

    /**
     * 今日UIp
     */
    private String todayUip;

    /**
     * 启用标识 0：启用 1：未启用
     */
    private Integer enableStatus;
}
