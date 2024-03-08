package com.shortlink.service.impl;

/**
 * @author LYT0905
 * @date 2024/03/08/15:40
 */

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.dao.entity.LinkDeviceStatsDO;
import com.shortlink.dao.mapper.LinkDeviceStatsMapper;
import com.shortlink.service.LinkDeviceStatsService;
import org.springframework.stereotype.Service;

/**
 * 监控访问设备类型接口实现层
 */
@Service
public class LinkDeviceStatsServiceImpl extends ServiceImpl<LinkDeviceStatsMapper, LinkDeviceStatsDO> implements LinkDeviceStatsService {
}
