package com.shortlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.dao.entity.LinkStatsTodayDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author LYT0905
 * @date 2024/03/09/14:58
 */

/**
 * 短链接监控之分页查询PV、UV、UIP持久层
 */
@Mapper
public interface LinkStatsTodayMapper extends BaseMapper<LinkStatsTodayDO> {
}
