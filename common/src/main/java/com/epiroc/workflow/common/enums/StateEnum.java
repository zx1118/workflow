package com.epiroc.workflow.common.enums;

/**
 * 工作流状态枚举
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-05-07
 */
public enum StateEnum {

    /* 待提交 */
    TOBRSUBMIT("-1", "TO_BE_SUBMIT"),

    /* 暂存 */
    DRAFT("0", "DRAFT"),

    /* 等待处理 */
    PENDING("1", "PENDING"),

    /* 完成 */
    COMPLETE("2", "COMPLETE"),

    /* 取消 */
    CANCEL("3", "CANCEL"),

    /* 拒绝 */
    REJECT("4", "REJECT"),

    /* 待重新提交 */
    RESUBMIT("5", "RESUBMIT"),

    /* 取消中 */
    CANCELLING("6", "CANCELLING"),


    ;

    private String code;

    private String status;

    StateEnum(String code, String status) {
        this.code = code;
        this.status = status;
    }

    /**
     * 获取枚举的 code 值
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取枚举的 status 值
     */
    public String getStatus() {
        return status;
    }

    /**
     * 根据 status（消息）获取对应的 code
     *
     * @param status 要查找的状态消息
     * @return 如果找到则返回对应的 code；否则返回 null
     */
    public static String getCodeByMessage(String status) {
        if (status == null) {
            return null;
        }
        for (StateEnum state : values()) {
            if (state.getStatus().equals(status)) {
                return state.getCode();
            }
        }
        return null;
    }

}
