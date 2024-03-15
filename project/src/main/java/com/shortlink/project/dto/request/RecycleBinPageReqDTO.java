package com.shortlink.project.dto.request;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.project.dao.entity.ShortLinkDO;
import lombok.Data;

import java.util.List;

/**
 * 回收站分页查询
 */
@Data
public class RecycleBinPageReqDTO extends Page<ShortLinkDO> {

    /**
     * 分组标识
     */
    private List<String> gidList;
}