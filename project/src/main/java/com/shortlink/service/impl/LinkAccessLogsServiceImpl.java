package com.shortlink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.dao.entity.LinkAccessLogsDO;
import com.shortlink.dao.mapper.LinkAccessLogsMapper;
import com.shortlink.service.LinkAccessLogsService;
import org.springframework.stereotype.Service;

/**
 * @author LYT0905
 * @date 2024/03/08/15:13
 */

@Service
public class LinkAccessLogsServiceImpl extends ServiceImpl<LinkAccessLogsMapper, LinkAccessLogsDO> implements LinkAccessLogsService {
}
