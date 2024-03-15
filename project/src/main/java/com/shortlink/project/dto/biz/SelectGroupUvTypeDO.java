package com.shortlink.project.dto.biz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author LYT0905
 * @date 2024/03/10/17:16
 */

/**
 * 查找分组用户的类型（新访客还是旧访客）实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SelectGroupUvTypeDO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 开始时间
     */
    private String startDate;

    /**
     * 结束时间
     */
    private String endDate;

    /**
     * 用户监控数据
     */
    private List<String> userAccessLogsList;
}
