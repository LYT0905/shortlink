package com.shortlink.dto.response;

import lombok.Data;

/**
 * @author LYT0905
 * @date 2024/03/01/16:45
 */

/**
 * 短链接分组响应数据
 */
@Data
public class ShortLinkGroupRespDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 创建分组用户名
     */
    private String username;

    /**
     * 分组排序
     */
    private Integer sortOrder;
}
