package com.shortlink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.dao.entity.LinkStatsTodayDO;
import com.shortlink.dao.mapper.LinkStatsTodayMapper;
import com.shortlink.service.LinkStatsTodayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author LYT0905
 * @date 2024/03/09/14:58
 */

/**
 * 短链接监控之分页查询PV、UV、UIP接口实现层
 */
@Service
@RequiredArgsConstructor
public class LinkStatsTodayServiceImpl extends ServiceImpl<LinkStatsTodayMapper, LinkStatsTodayDO> implements LinkStatsTodayService {
}
