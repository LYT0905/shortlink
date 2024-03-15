package com.shortlink.admin.dto.response;

/**
 * @author LYT0905
 * @date 2024/02/27/17:27
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录数据响应实体
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRespDTO {
    /**
     * 用户登录token
     */
   private String token;
}
