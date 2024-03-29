package com.shortlink.project.dto.request;

import lombok.Data;

/**
 * @author LYT0905
 * @date 2024/03/07/17:30
 */

@Data
public class RecycleBinRecoverReqDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
