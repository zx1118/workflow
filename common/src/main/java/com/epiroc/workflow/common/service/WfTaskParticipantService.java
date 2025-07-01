package com.epiroc.workflow.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epiroc.workflow.common.entity.WfTaskParticipant;

import java.util.List;

public interface WfTaskParticipantService extends IService<WfTaskParticipant> {

    Integer saveSubmitFlow(List<WfTaskParticipant> flowList, Integer orderId);

    WfTaskParticipant updateCurrentTaskAndReturnNext(WfTaskParticipant currentTaskParticipant, List<WfTaskParticipant> wfTaskParticipantList);

    List<WfTaskParticipant> getFullFlow(Integer orderId);

    WfTaskParticipant updateCurrent(WfTaskParticipant current);

    WfTaskParticipant updateAndReturnNext(WfTaskParticipant current, List<WfTaskParticipant> wfTaskParticipantList);

}
