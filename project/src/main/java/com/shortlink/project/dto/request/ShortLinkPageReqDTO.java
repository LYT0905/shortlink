package com.shortlink.project.dto.request;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.project.dao.entity.ShortLinkDO;
import lombok.Data;

/**
 * @author LYT0905
 * @date 2024/03/04/18:01
 */

@Data
public class ShortLinkPageReqDTO extends Page<ShortLinkDO> {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 排序标识
     */
    private String orderTag;
}
