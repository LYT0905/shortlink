package com.shortlink.dto.request;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.dao.entity.LinkAccessLogsDO;
import lombok.Data;

/**
 * @author LYT0905
 * @date 2024/03/10/17:07
 */

@Data
public class ShortLinkGroupStatsAccessRecordReqDTO extends Page<LinkAccessLogsDO> {
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
