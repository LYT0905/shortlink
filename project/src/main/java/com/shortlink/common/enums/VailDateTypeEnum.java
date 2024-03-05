package com.shortlink.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author LYT0905
 * @date 2024/03/05/18:09
 */

/**
 * 有效期类型
 */
@RequiredArgsConstructor
public enum VailDateTypeEnum {

    /**
     * 永久有效期
     */
    PERMANENT(0),

    /**
     * 自定义有效期
     */
    CUSTOM(1);

    @Getter
    private final int type;
}
