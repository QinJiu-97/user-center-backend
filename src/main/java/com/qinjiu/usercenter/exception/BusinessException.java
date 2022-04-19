package com.qinjiu.usercenter.exception;

import com.qinjiu.usercenter.common.BaseResponse;
import com.qinjiu.usercenter.common.ErrorCode;

/**
 * @author QinJiu
 * @Date 2022/4/14
 */
public class BusinessException extends RuntimeException{

    private final int code;

    private final String description;

    public BusinessException(String message,  int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode,String description){
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
