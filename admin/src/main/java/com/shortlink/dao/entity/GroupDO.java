package com.shortlink.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shortlink.common.datatbase.BaseDO;
import lombok.Data;

/**
 * @author LYT0905
 * @date 2024/03/01/16:02
 */

@Data
@TableName("t_group")
public class GroupDO extends BaseDO {
    /**
     * id
     */
    private Long id;

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
