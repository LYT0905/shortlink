package com.shortlink.admin.dto.request;

import lombok.Data;

/**
 * @author LYT0905
 * @date 2024/02/29/12:54
 */

/**
 * 用户信息修改响应实体参数
 */
@Data
public class UserUpdateReqDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;
}
