package com.shortlink.toolkit;

/**
 * @author LYT0905
 * @date 2024/03/06/20:45
 */

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.Optional;

import static com.shortlink.common.constant.ShortLinkConstant.DEFAULT_CACHE_VALID_TIME;

/**
 * 短链接工具类
 */
public class LinkUtil {

    /**
     * 判断短链接是否永久有效
     * @param validDate
     * @return
     */
    public static long getLinkCacheValidDateTime(Date validDate){
        // 判断有效期是否是永久有效
        // 如果不是则直接通过当前时间进行时间后移
        return Optional.ofNullable(validDate)
                .map(each -> DateUtil.between(new Date(), each, DateUnit.MS))
                .orElse(DEFAULT_CACHE_VALID_TIME);
    }
}
