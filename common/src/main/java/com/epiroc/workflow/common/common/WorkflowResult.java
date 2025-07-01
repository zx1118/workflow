package com.epiroc.workflow.common.common;

import com.epiroc.workflow.common.enums.ResultEnum;
import com.epiroc.workflow.common.system.constant.CommonConstant;
import lombok.Data;

import java.io.Serializable;

/**
 *   接口返回数据格式
 * @author scott
 * @email jeecgos@163.com
 * @date  2019年1月19日
 */
@Data
// @ApiModel(value="接口返回对象", description="接口返回对象")
public class WorkflowResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功标志
     */
    // @ApiModelProperty(value = "成功标志")
    private boolean success = true;

    /**
     * 返回处理消息
     */
    // @ApiModelProperty(value = "返回处理消息")
    private String message = "操作成功！";

    /**
     * 返回代码
     */
    // @ApiModelProperty(value = "返回代码")
    private Integer code = 0;

    /**
     * 返回数据对象 data
     */
    // @ApiModelProperty(value = "返回数据对象")
    private T result;

    /**
     * 时间戳
     */
    // @ApiModelProperty(value = "时间戳")
    private long timestamp = System.currentTimeMillis();

    public WorkflowResult() {

    }

    public WorkflowResult<T> success(String message) {
        this.message = message;
        this.code = CommonConstant.SC_OK_200;
        this.success = true;
        return this;
    }


    public static WorkflowResult<Object> ok() {
        WorkflowResult<Object> r = new WorkflowResult<Object>();
        r.setSuccess(true);
        r.setCode(CommonConstant.SC_OK_200);
        r.setMessage("成功");
        return r;
    }

    public static WorkflowResult<Object> ok(String msg) {
        WorkflowResult<Object> r = new WorkflowResult<Object>();
        r.setSuccess(true);
        r.setCode(CommonConstant.SC_OK_200);
        r.setMessage(msg);
        return r;
    }

    public static WorkflowResult<Object> ok(String msg, Object data) {
        WorkflowResult<Object> r = new WorkflowResult<Object>();
        r.setSuccess(true);
        r.setCode(CommonConstant.SC_OK_200);
        r.setMessage(msg);
        r.setResult(data);
        return r;
    }

    public static WorkflowResult<Object> ok(Object data) {
        WorkflowResult<Object> r = new WorkflowResult<Object>();
        r.setSuccess(true);
        r.setCode(CommonConstant.SC_OK_200);
        r.setResult(data);
        return r;
    }

    public static WorkflowResult<Object> error(String msg) {
        return error(CommonConstant.SC_INTERNAL_SERVER_ERROR_500, msg);
    }

    public static WorkflowResult<Object> error(int code, String msg) {
        WorkflowResult<Object> r = new WorkflowResult<Object>();
        r.setCode(code);
        r.setMessage(msg);
        r.setSuccess(false);
        return r;
    }

    public WorkflowResult<T> error500(String message) {
        this.message = message;
        this.code = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;
        this.success = false;
        return this;
    }
    /**
     * 无权限访问返回结果
     */
    public static WorkflowResult<Object> noauth(String msg) {
        return error(CommonConstant.SC_JEECG_NO_AUTHZ, msg);
    }

    public WorkflowResult<T> errorcm(Integer code, String msg) {
        this.message = msg;
        this.code =code;
        this.success = false;
        return this;
    }

    public WorkflowResult(boolean success, ResultEnum resultEnum) {
        this.success = success;
        this.code = success ? ResultEnum.SUCCESS.getCode()
                : (resultEnum == null ? ResultEnum.COMMON_FAIL.getCode() : resultEnum.getCode());
        this.message = success ? ResultEnum.SUCCESS.getMessage()
                : (resultEnum == null ? ResultEnum.COMMON_FAIL.getMessage() : resultEnum.getMessage());
    }

    public WorkflowResult(ResultEnum resultEnum) {
        this.success = true;
        this.code = resultEnum.getCode();
        this.message = resultEnum.getMessage();
    }


    public static WorkflowResult error(ResultEnum resultEnum, Object data) {
        WorkflowResult<Object> r = new WorkflowResult<Object>(false, resultEnum);
        r.setResult(data);
        return r;
    }

    public static WorkflowResult ok(ResultEnum resultEnum) {
        WorkflowResult<Object> r = new WorkflowResult<Object>(resultEnum);
        return r;
    }

}