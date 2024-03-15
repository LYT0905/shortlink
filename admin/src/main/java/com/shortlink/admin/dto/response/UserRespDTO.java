package com.shortlink.admin.dto.response;

/**
 * @author LYT0905
 * @date 2024/02/27/17:27
 */

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.shortlink.admin.common.serialize.PhoneDesensitizationSerializer;
import lombok.Data;

/**
 * 用户数据响应实体
 */

@Data
public class UserRespDTO {
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
     * 手机号, 将手机号进行脱敏
     */
    @JsonSerialize(using = PhoneDesensitizationSerializer.class)
    private String phone;

    /**
     * 邮箱
     */
    private String mail;
}
