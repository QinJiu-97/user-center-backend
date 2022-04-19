package com.qinjiu.usercenter.exception;

import com.qinjiu.usercenter.common.BaseResponse;
import com.qinjiu.usercenter.common.ErrorCode;
import com.qinjiu.usercenter.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author QinJiu
 * @Date 2022/4/16
 *
 * 自定义全局异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e){
        log.info("BusinessException--->" + e.getMessage(),e);
        return ResultUtils.error(ErrorCode.PARAM_ERROR, e.getDescription());

    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(RuntimeException e){
        log.info("runtimeException--->",e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(),"");
    }
}
