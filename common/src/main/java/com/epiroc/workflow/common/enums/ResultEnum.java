package com.epiroc.workflow.common.enums;

import lombok.Getter;

/**
 * @author : Theo Zheng
 * @version : V1.0
 * @project : antbi
 * @file : ResultEnum
 * @description : 结果Enum
 * @date : 2020/10/28 10:30
 * @Copyright : 2020 Epiroc Trading Co., Ltd. All rights reserved.
 */
@Getter
public enum ResultEnum {

    /* 成功 */
    SUCCESS(200, "成功"),

    /* 默认失败 */
    COMMON_FAIL(999, "失败"),

    IO_ERROR(500, "网络异常"),

    USERNAME_PASSWORD_ERROR(49, "用户名或密码错误"),

    AUTHENTICATE_ERROR(50, "认证出错"),

    FLOW_OVER(201, "关联成功，流程结束！"),

    FLOW_NOT_OVER(300, "关联成功，流程未结束！")
    ;

    private Integer code;

    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
