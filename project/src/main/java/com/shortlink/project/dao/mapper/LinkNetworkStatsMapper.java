package com.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.project.dao.entity.LinkNetworkStatsDO;
import com.shortlink.project.dto.request.ShortLinkGroupStatsReqDTO;
import com.shortlink.project.dto.request.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author LYT0905
 * @date 2024/03/08/19:48
 */

/**
 * 监控网络持久层
 */
@Mapper
public interface LinkNetworkStatsMapper extends BaseMapper<LinkNetworkStatsDO> {

    /**
     * 记录访问设备监控数据
     */
    @Insert("INSERT INTO " +
            "t_link_network_stats (full_short_url, date, cnt, network, create_time, update_time, del_flag) " +
            "VALUES( #{linkNetworkStats.fullShortUrl}, #{linkNetworkStats.date}, #{linkNetworkStats.cnt}, #{linkNetworkStats.network}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkNetworkStats.cnt};")
    void shortLinkNetworkState(@Param("linkNetworkStats") LinkNetworkStatsDO linkNetworkStatsDO);

    /**
     * 根据短链接获取指定日期内访问网络监控数据
     */
    @Select("SELECT " +
            "    tlns.network, " +
            "    SUM(tlns.cnt) AS cnt " +
            "FROM " +
            "    t_link tl INNER JOIN " +
            "    t_link_network_stats tlns ON tl.full_short_url = tlns.full_short_url " +
            "WHERE " +
            "    tlns.full_short_url = #{param.fullShortUrl} " +
            "    AND tl.gid = #{param.gid} " +
            "    AND tl.del_flag = '0' " +
            "    AND tl.enable_status = #{param.enableStatus} " +
            "    AND tlns.date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    tlns.full_short_url, tl.gid, tlns.network;")
    List<LinkNetworkStatsDO> listNetworkStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组获取指定日期内访问网络监控数据
     */
    @Select("SELECT " +
            "    tlns.network, " +
            "    SUM(tlns.cnt) AS cnt " +
            "FROM " +
            "    t_link tl INNER JOIN " +
            "    t_link_network_stats tlns ON tl.full_short_url = tlns.full_short_url " +
            "WHERE " +
            "    tl.gid = #{param.gid} " +
            "    AND tl.del_flag = '0' " +
            "    AND tl.enable_status = '0' " +
            "    AND tlns.date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    tl.gid, tlns.network;")
    List<LinkNetworkStatsDO> listNetworkStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}
