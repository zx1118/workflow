package com.epiroc.workflow.common.enums;

/**
 * 任务状态枚举
 */
public enum TaskStatusEnum {
    
    /**
     * 未审批
     */
    NOT_APPROVED("0", "未审批"),
    
    /**
     * 等待操作
     */
    WAITING("1", "等待操作"),
    
    /**
     * 已审批
     */
    APPROVED("2", "已审批"),
    
    /**
     * 取消
     */
    CANCELLED("3", "取消"),
    
    /**
     * 关单
     */
    CLOSED("4", "关单");
    
    private final String code;
    private final String description;
    
    TaskStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取枚举
     */
    public static TaskStatusEnum getByCode(String code) {
        for (TaskStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的任务状态代码: " + code);
    }
} 