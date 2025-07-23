package com.epiroc.workflow.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epiroc.workflow.common.entity.WfFlow;
import com.epiroc.workflow.common.system.flow.entity.FlowParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface WfFlowMapper extends BaseMapper<WfFlow> {

    WfFlow selectUnionParticipantId(@Param("param") FlowParam param);

    List<Integer> getWfFlowIdList(@Param("param") FlowParam param);

    List<Map<String, Object>> executeQuerySql(String sql);

}
