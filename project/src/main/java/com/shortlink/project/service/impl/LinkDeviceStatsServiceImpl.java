package com.shortlink.project.service.impl;

/**
 * @author LYT0905
 * @date 2024/03/08/15:40
 */

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.project.dao.entity.LinkDeviceStatsDO;
import com.shortlink.project.dao.mapper.LinkDeviceStatsMapper;
import com.shortlink.project.service.LinkDeviceStatsService;
import org.springframework.stereotype.Service;

/**
 * 监控访问设备类型接口实现层
 */
@Service
public class LinkDeviceStatsServiceImpl extends ServiceImpl<LinkDeviceStatsMapper, LinkDeviceStatsDO> implements LinkDeviceStatsService {
}
