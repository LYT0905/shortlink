package com.shortlink.project.dto.response;

import lombok.Data;

/**
 * @author LYT0905
 * @date 2024/03/05/17:00
 */

/**
 * 查询短链接数量返回实体
 */
@Data
public class ShortLinkGroupRespDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 当前组下短链接数量
     */
    private Integer shortLinkCount;
}
