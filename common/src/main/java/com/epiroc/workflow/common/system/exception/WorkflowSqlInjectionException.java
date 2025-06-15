package com.epiroc.workflow.common.system.exception;

/**
 * @author : Theo Zheng
 * @version : V1.0
 * @project : antbi
 * @file : ParetoSqlInjectionException
 * @description : 自定义SQL注入异常
 * @date : 2024/6/21 18:45
 * @Copyright : 2024 Epiroc Trading Co., Ltd. All rights reserved.
 */
public class WorkflowSqlInjectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public WorkflowSqlInjectionException(String message){
        super(message);
    }

    public WorkflowSqlInjectionException(Throwable cause)
    {
        super(cause);
    }

    public WorkflowSqlInjectionException(String message, Throwable cause)
    {
        super(message,cause);
    }
}
