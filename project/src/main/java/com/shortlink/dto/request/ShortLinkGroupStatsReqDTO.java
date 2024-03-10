package com.shortlink.dto.request;

import lombok.Data;

/**
 * @author LYT0905
 * @date 2024/03/10/16:30
 */

@Data
public class ShortLinkGroupStatsReqDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;
}
