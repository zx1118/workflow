package com.epiroc.workflow.common.system.flow;

import com.epiroc.workflow.common.service.WfFlowService;

public class FlowDecoratorFactory {

    public static IFlow createFlow(String flowType, IFlow flow, WfFlowService wfFlowService, Integer index) {
        switch (flowType) {
            case "0":
                flow = new FixedFlow(flow, wfFlowService, index);
                break;
            case "1":
                flow = new ProkuraFlow(flow, wfFlowService, index);
                break;
            case "2":
                flow = new SetFlow(flow, wfFlowService, index);
                break;
            case "3":
                flow = new OrganizationalStructureFlow(flow, wfFlowService, index);
                break;
            default:
                break;
        }
        return flow;
    }

}
