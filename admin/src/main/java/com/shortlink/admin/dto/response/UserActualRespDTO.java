package com.shortlink.admin.dto.response;

/**
 * @author LYT0905
 * @date 2024/02/27/17:27
 */

import lombok.Data;

/**
 * 用户数据响应实体
 */

@Data
public class UserActualRespDTO {
    /**
     * id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号, 无脱敏
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;
}
