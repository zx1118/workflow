package com.epiroc.workflow.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epiroc.workflow.common.common.Result;
import com.epiroc.workflow.common.entity.WfTaskParticipant;
import com.epiroc.workflow.common.entity.form.GetFlowForm;
import com.epiroc.workflow.common.entity.WfFlow;
import com.epiroc.workflow.common.system.flow.entity.FlowParam;

import java.util.List;

public interface WfFlowService extends IService<WfFlow>  {

    /**
     * 根据 entity 条件，查询记录
     * @param wfFlow
     * @return
     */
    List<WfFlow> queryWfFlowList(WfFlow wfFlow);

    /**
     * 获取流程信息
     * @param getFlowForm
     * @return
     */
    Result getFlow(GetFlowForm getFlowForm);

    /**
     * 根据wf_process_id,flow_type,index,stage查询流程信息
     * @param param
     * @return
     */
    List<Integer> getParticipantIdList(FlowParam param);

    List<Integer> getFixedParticipantIdList(FlowParam param);

    List<Integer> getProkuraParticipantIdList(FlowParam param);

    /**
     * 获取流程详情
     * @param param
     * @return
     */
    List<WfFlow> getFlowDetail(FlowParam param);

    List<Integer> getProkuraWfFlowIdList(FlowParam param);

    List<WfFlow> getProkuraFlowDetail(FlowParam param);

    List<WfFlow> getSetFlowDetail(FlowParam param);

    List<Integer> getSetWfFlowIdList(FlowParam param);

    List<Integer> getWfFlowIdList(FlowParam param);

    List<WfTaskParticipant> dealSubmitFlow(List<WfTaskParticipant> flowList, Integer orderId);

}
