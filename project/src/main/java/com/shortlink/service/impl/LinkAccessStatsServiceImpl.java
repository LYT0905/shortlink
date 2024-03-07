package com.shortlink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.dao.entity.LinkAccessStatsDO;
import com.shortlink.dao.mapper.LinkAccessStatsMapper;
import com.shortlink.service.LinkAccessStatsService;
import org.springframework.stereotype.Service;

/**
 * @author LYT0905
 * @date 2024/03/07/20:34
 */

/**
 * 基础访问接口实现层
 */
@Service
public class LinkAccessStatsServiceImpl extends ServiceImpl<LinkAccessStatsMapper, LinkAccessStatsDO> implements LinkAccessStatsService {
}
