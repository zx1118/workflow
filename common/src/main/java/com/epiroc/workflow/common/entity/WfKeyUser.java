package com.epiroc.workflow.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : Theo Zheng
 * @version : V1.0
 * @project : antbi
 * @file : WfKeyUser
 * @description : 工作流 key user 实体类
 * @date : 2021-10-12 18:05
 * @Copyright : 2021 Epiroc Trading Co., Ltd. All rights reserved.
 */
@Data
public class WfKeyUser implements Serializable {

    private static final long serialVersionUID = 7819396632436342364L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String field;

    private String fieldCn;

    private String unitId;

    private String guid;

    private String name;

    private String email;

    private String applicationCode;

    private String requestType;

    private String foreignId;

    private String remarks;

    private String delFlag;

    private String createBy;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String updateBy;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;


}
