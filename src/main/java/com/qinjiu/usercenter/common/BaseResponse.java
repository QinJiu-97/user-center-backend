package com.qinjiu.usercenter.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author QinJiu
 * @Date 2022/4/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    private String description;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(),null,errorCode.getMessage(),errorCode.getDescription());
    }

}
