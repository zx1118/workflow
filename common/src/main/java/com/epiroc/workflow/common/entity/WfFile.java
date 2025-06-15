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
 * @file : WfFile
 * @description : TODO
 * @date : 2021-10-20 13:27
 * @Copyright : 2021 Epiroc Trading Co., Ltd. All rights reserved.
 */
@Data
public class WfFile implements Serializable {

    private static final long serialVersionUID = -8274695417717086474L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String orderId;

    private String taskId;

    private String url;

    private String fileName;

    private String type;

    private String originalFileName;

    private String creator;

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
