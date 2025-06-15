package com.epiroc.workflow.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epiroc.workflow.common.entity.WfRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface WfRuleMapper extends BaseMapper<WfRule> {

    List<Map<String, Object>> executeSql(@Param("sql") String executeSql);

}
