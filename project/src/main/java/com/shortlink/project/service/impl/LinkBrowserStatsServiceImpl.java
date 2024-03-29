package com.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.project.dao.entity.LinkBrowserStatsDO;
import com.shortlink.project.dao.mapper.LinkBrowserStatsMapper;
import com.shortlink.project.service.LinkBrowserStatsService;
import org.springframework.stereotype.Service;

/**
 * @author LYT0905
 * @date 2024/03/08/14:50
 */

/**
 * 浏览器访问数据接口实现层
 */
@Service
public class LinkBrowserStatsServiceImpl extends ServiceImpl<LinkBrowserStatsMapper, LinkBrowserStatsDO> implements LinkBrowserStatsService {
}
