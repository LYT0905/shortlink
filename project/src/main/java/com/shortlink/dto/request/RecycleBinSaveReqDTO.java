package com.shortlink.dto.request;

/**
 * @author LYT0905
 * @date 2024/03/07/12:57
 */

import lombok.Data;

/**
 * 移至短链接回收站请求参数
 */

@Data
public class RecycleBinSaveReqDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
