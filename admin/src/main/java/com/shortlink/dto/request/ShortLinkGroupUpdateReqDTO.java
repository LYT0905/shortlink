package com.shortlink.dto.request;

import lombok.Data;

/**
 * @author LYT0905
 * @date 2024/03/01/16:02
 */

@Data
public class ShortLinkGroupUpdateReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

}
