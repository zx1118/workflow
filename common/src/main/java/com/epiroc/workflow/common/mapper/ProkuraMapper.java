package com.epiroc.workflow.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epiroc.workflow.common.entity.Prokura;
import com.epiroc.workflow.common.system.flow.entity.FlowParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProkuraMapper extends BaseMapper<Prokura> {

    List<Prokura> getProkuraParticipants(@Param("param") FlowParam param);

}
