package com.shortlink.project.common.enums;

import com.shortlink.project.common.convention.errorcode.IErrorCode;

/**
 * @author LYT0905
 * @date 2024/03/04/12:36
 */
public enum ShortLinkErrorCodeEnums implements IErrorCode {

    SHORT_LINK_CREATE_ERROR("B0000300", "短链接创建失败"),
    SHORT_LINK_INSERT_ERROR("B0000300", "短链接生成重复");

    private final String code;

    private final String message;

    ShortLinkErrorCodeEnums(String code, String message) {
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
