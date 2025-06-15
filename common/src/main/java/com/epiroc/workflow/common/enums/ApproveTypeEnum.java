package com.epiroc.workflow.common.enums;

/**
 * 审批类型枚举
 */
public enum ApproveTypeEnum {
    
    /**
     * 同意
     */
    APPROVE("0", "同意"),
    
    /**
     * 拒绝/退回
     */
    REJECT("1", "拒绝"),
    
    /**
     * 取消
     */
    CANCEL("2", "取消"),
    
    /**
     * 关单
     */
    CLOSE("3", "关单");
    
    private final String code;
    private final String description;
    
    ApproveTypeEnum(String code, String description) {
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
    public static ApproveTypeEnum getByCode(String code) {
        for (ApproveTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的审批类型代码: " + code);
    }
} 