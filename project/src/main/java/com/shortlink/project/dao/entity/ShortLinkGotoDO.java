package com.shortlink.project.dao.entity;

/**
 * @author LYT0905
 * @date 2024/03/06/12:36
 */

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接跳转实体
 */

@TableName("t_link_goto")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkGotoDO {
    /**
     * ID
     */
    private Integer id;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
