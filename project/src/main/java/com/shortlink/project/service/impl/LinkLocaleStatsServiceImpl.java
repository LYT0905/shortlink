package com.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.project.dao.entity.LinkLocaleStatsDO;
import com.shortlink.project.dao.mapper.LinkLocaleStatsMapper;
import com.shortlink.project.service.LinkLocaleStatsService;
import org.springframework.stereotype.Service;

/**
 * @author LYT0905
 * @date 2024/03/08/13:27
 */

@Service
public class LinkLocaleStatsServiceImpl extends ServiceImpl<LinkLocaleStatsMapper, LinkLocaleStatsDO> implements LinkLocaleStatsService {
}
