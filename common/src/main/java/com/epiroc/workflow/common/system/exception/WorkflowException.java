package com.epiroc.workflow.common.system.exception;

import com.epiroc.workflow.common.system.constant.CommonConstant;

/**
 * @author : Theo Zheng
 * @version : V1.0
 * @project : antbi
 * @file : ParetoException
 * @description : Pareto 自定义异常
 * @date : 2024/6/19 15:06
 * @Copyright : 2024 Epiroc Trading Co., Ltd. All rights reserved.
 */
public class WorkflowException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 返回给前端的错误code
     */
    private int errCode = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;

    public WorkflowException(String message){
        super(message);
    }

    public WorkflowException(String message, int errCode){
        super(message);
        this.errCode = errCode;
    }

    public int getErrCode() {
        return errCode;
    }

    public WorkflowException(Throwable cause)
    {
        super(cause);
    }

    public WorkflowException(String message, Throwable cause)
    {
        super(message,cause);
    }

}
