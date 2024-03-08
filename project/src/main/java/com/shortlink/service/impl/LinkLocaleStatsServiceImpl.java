package com.shortlink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.dao.entity.LinkLocaleStatsDO;
import com.shortlink.dao.mapper.LinkLocaleStatsMapper;
import com.shortlink.service.LinkLocaleStatsService;
import org.springframework.stereotype.Service;

/**
 * @author LYT0905
 * @date 2024/03/08/13:27
 */

@Service
public class LinkLocaleStatsServiceImpl extends ServiceImpl<LinkLocaleStatsMapper, LinkLocaleStatsDO> implements LinkLocaleStatsService {
}
