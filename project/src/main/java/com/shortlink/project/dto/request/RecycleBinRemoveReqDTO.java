package com.shortlink.project.dto.request;

import lombok.Data;

/**
 * @author LYT0905
 * @date 2024/03/07/18:10
 */

@Data
public class RecycleBinRemoveReqDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整链接
     */
    private String fullShortUrl;
}
