package com.shortlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.dao.entity.ShortLinkDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author LYT0905
 * @date 2024/03/07/12:59
 */

@Mapper
public interface RecycleBinMapper extends BaseMapper<ShortLinkDO> {
}
