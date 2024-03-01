package com.shortlink.dto.request;

import lombok.Data;

/**
 * @author LYT0905
 * @date 2024/03/01/16:02
 */

@Data
public class ShortLinkGroupUpdateSortReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 排序
     */
    private Integer sortOrder;

}
