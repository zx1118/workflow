package com.epiroc.workflow.common.system.exception;

// 自定义异常类
public class DynamicServiceException extends RuntimeException {
    private final String errorCode;

    public DynamicServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DynamicServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
