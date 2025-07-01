package com.epiroc.workflow.common.convert;

import com.epiroc.workflow.common.entity.WfFlow;
import com.epiroc.workflow.common.entity.WfTaskParticipant;
import com.epiroc.workflow.common.system.constant.CommonConstant;
import com.epiroc.workflow.common.system.constant.WorkflowConstant;
import com.epiroc.workflow.common.util.oConvertUtils;

import java.util.List;
import java.util.Map;

public class WfFlow2WfTaskParticipant implements WorkflowConstant, CommonConstant {

    public static List<WfTaskParticipant> getSubmitWfTaskParticipants(List<WfFlow> flowList, Map<String, Object> assigneeMap) {
        for (int i = flowList.size() - 1; i >= 0; i--) {
            WfFlow wfFlow = flowList.get(i);
            if(!FLOW_TYPE_FIXED.equals(wfFlow.getFlowType())){   // 固定流程不要设置审批人;其他类型流程设置审批人
                String field = wfFlow.getField();
                if(assigneeMap.containsKey(field)){
                    String guid = assigneeMap.get(field + UNIT_SHORT_LINE_UNDER + STRING_NAME_GUID).toString();
                    String email = assigneeMap.get(field + UNIT_SHORT_LINE_UNDER + STRING_EMAIL).toString();
                    String name = assigneeMap.get(field).toString();
                    wfFlow.setOperator(name);
                    wfFlow.setOperatorId(guid);
                    wfFlow.setOperatorEmail(email);
                } else {
                    flowList.remove(wfFlow);
                }
            }
        }
        List<WfTaskParticipant> resultList = oConvertUtils.entityListToModelList(flowList, WfTaskParticipant.class);
        for(int j=0;j<resultList.size();j++){
            WfTaskParticipant wfTaskParticipant = resultList.get(j);
            wfTaskParticipant.setWfFlowId(wfTaskParticipant.getId());
            wfTaskParticipant.setId(null);
        }
        return resultList;
    }

}
