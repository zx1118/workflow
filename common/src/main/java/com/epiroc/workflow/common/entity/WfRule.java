package com.epiroc.workflow.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class WfRule {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String tableName;

    private String executeSql;

    private String needSetField;

    private Integer type;

}
