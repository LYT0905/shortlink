package com.shortlink.common.enums;

import com.shortlink.common.convention.errorcode.IErrorCode;

/**
 * @author LYT0905
 * @date 2024/02/27/18:07
 */
public enum UserErrorCodeEnums implements IErrorCode {

    USER_NULL("B000200", "用户记录不存在"),
    USER_NAME_EXIST("B000201", "用户名已存在"),
    USER_EXIST("B000202", "用户记录已存在"),
    USER_SAVE_ERROR("B000203", "用户记录新增失败"),
    USER_LOGIN_ERROR("B000204", "用户登录失败,用户名或密码错误"),
    USER_HAS_LOGIN("B000205", "用户已登录"),
    USER_LOGOUT_ERROR("B000206", "用户名错误或者Token标识过期");


    private final String code;

    private final String message;

    UserErrorCodeEnums(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
