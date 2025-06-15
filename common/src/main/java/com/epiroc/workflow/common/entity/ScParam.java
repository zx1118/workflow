package com.epiroc.workflow.common.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 供应商参数表(sc_param)实体类
 */
@Data
@TableName("sc_param")
public class ScParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 地址
     */
    private String address;

    /**
     * 附件
     */
    private List<String> attachments;

    /**
     * 银行账户
     */
    @TableField("bank_account")
    private String bankAccount;

    /**
     * 银行名称
     */
    @TableField("bank_name")
    private String bankName;

    /**
     * 是否全新
     */
    private Boolean brandnew;

    /**
     * 联系人
     */
    private String contact;

    /**
     * 货币
     */
    private String currency;

    /**
     * 电子邮件
     */
    private String email;

    /**
     * 付款条款
     */
    @TableField("payment_term")
    private String paymentTerm;

    /**
     * 付款类型
     */
    @TableField("payment_type")
    private String paymentType;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮政编码
     */
    private String postcode;

    /**
     * 请求人邮箱
     */
    private String requester;

    /**
     * 请求人姓名
     */
    @TableField("requester_name")
    private String requesterName;

    /**
     * 请求日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("request_date")
    private Date requestDate;

    /**
     * 供应商名称
     */
    @TableField("supplier_name")
    private String supplierName;

    /**
     * 税号
     */
    private String taxNo;

    /**
     * 单位
     */
    private String unit;

    @TableField("sap_code")
    private String sapCode;

    @Override
    public String toString() {
        return "ScParam{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", attachments=" + attachments +
                ", bankAccount='" + bankAccount + '\'' +
                ", bankName='" + bankName + '\'' +
                ", brandnew=" + brandnew +
                ", contact='" + contact + '\'' +
                ", currency='" + currency + '\'' +
                ", email='" + email + '\'' +
                ", paymentTerm='" + paymentTerm + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", phone='" + phone + '\'' +
                ", postcode='" + postcode + '\'' +
                ", requester='" + requester + '\'' +
                ", requesterName='" + requesterName + '\'' +
                ", requestDate=" + requestDate +
                ", supplierName='" + supplierName + '\'' +
                ", taxNo='" + taxNo + '\'' +
                ", unit='" + unit + '\'' +
                '}';
    }
}
