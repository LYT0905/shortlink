package com.shortlink.remote.dto.response;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * @author LYT0905
 * @date 2024/03/11/13:05
 */

/**
 * 短链接基础信息响应参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkBaseInfoRespDTO {
    /**
     * 描述信息
     */
    @ExcelProperty("标题")
    @ColumnWidth(40)
    private String describe;

    /**
     * 短链接
     */
    @ExcelProperty("短链接")
    @ColumnWidth(40)
    private String fullShortUrl;

    /**
     * 原始链接
     */
    @ExcelProperty("原始链接")
    @ColumnWidth(80)
    private String originUrl;
}
