package com.shortlink.dao.mapper;

/**
 * @author LYT0905
 * @date 2024/03/08/15:11
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.dao.entity.LinkAccessLogsDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 访问日志监控持久层
 */
@Mapper
public interface LinkAccessLogsMapper extends BaseMapper<LinkAccessLogsDO> {
}
