package com.qinjiu.usercenter.common;

import lombok.Data;

/**
 * @author QinJiu
 * @Date 2022/4/14
 */

public enum ErrorCode {
    /**
     * 错误类型设置
     */
    PARAM_ERROR(40000,"请求参数异常",""),
    NULL_ERROR(40001,"请求数据为空",""),
    NOT_LOGIN(40100,"未登录",""),
    NO_AUTH(40101,"无权限",""),
    FORBIDDEN(40301,"禁止操作",""),
    SYSTEM_ERROR(50000,"系统内部异常","");

    private final int code;
    private final String message;
    private final String description;



    public String getMessage() {
        return message;
    }

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }



    public String getDescription() {
        return description;
    }
}
