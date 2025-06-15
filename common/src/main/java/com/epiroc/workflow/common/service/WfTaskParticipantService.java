package com.epiroc.workflow.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epiroc.workflow.common.entity.WfTaskParticipant;

import java.util.List;

public interface WfTaskParticipantService extends IService<WfTaskParticipant> {

    Integer saveSubmitFlow(List<WfTaskParticipant> flowList, Integer orderId);

    WfTaskParticipant updateCurrentTaskAndReturnNext(WfTaskParticipant wfTaskParticipant, List<WfTaskParticipant> wfTaskParticipantList);

}
