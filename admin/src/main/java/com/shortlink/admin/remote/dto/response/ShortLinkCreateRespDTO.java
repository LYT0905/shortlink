package com.shortlink.admin.remote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LYT0905
 * @date 2024/03/03/21:58
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShortLinkCreateRespDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 短链接
     */
    private String shortUri;

    /**
     * 原始链接
     */
    private String originUrl;
}
