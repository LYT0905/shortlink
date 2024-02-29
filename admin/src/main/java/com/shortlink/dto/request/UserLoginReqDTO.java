package com.shortlink.dto.request;

import lombok.Data;

/**
 * @author LYT0905
 * @date 2024/02/29/12:54
 */

/**
 * 用户登录响应实体参数
 */
@Data
public class UserLoginReqDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
