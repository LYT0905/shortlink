package com.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortlink.project.dto.request.ShortLinkGroupStatsAccessRecordReqDTO;
import com.shortlink.project.dto.request.ShortLinkGroupStatsReqDTO;
import com.shortlink.project.dto.request.ShortLinkStatsAccessRecordReqDTO;
import com.shortlink.project.dto.request.ShortLinkStatsReqDTO;
import com.shortlink.project.dto.response.ShortLinkStatsAccessRecordRespDTO;
import com.shortlink.project.dto.response.ShortLinkStatsRespDTO;

/**
 * 短链接监控接口层
 */
public interface ShortLinkStatsService {

    /**
     * 获取单个短链接监控数据
     *
     * @param requestParam 获取短链接监控数据入参
     * @return 短链接监控数据
     */
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam);

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam);

    /**
     * 访问分组短链接指定时间内监控数据
     */
    ShortLinkStatsRespDTO groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam);

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     */
    IPage<ShortLinkStatsAccessRecordRespDTO> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam);
}