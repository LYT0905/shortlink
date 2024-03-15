package com.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.project.dao.entity.LinkNetworkStatsDO;
import com.shortlink.project.dao.mapper.LinkNetworkStatsMapper;
import com.shortlink.project.service.LinkNetworkStatsService;
import org.springframework.stereotype.Service;

/**
 * @author LYT0905
 * @date 2024/03/08/19:48
 */

/**
 * 监控网络接口实现层
 */
@Service
public class LinkNetworkStatsServiceImpl extends ServiceImpl<LinkNetworkStatsMapper, LinkNetworkStatsDO> implements LinkNetworkStatsService {
}
