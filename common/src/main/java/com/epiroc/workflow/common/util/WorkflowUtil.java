package com.epiroc.workflow.common.util;

import com.epiroc.workflow.common.entity.WfTaskParticipant;
import com.epiroc.workflow.common.system.constant.WorkflowConstant;

import java.util.List;
import java.util.Objects;

public class WorkflowUtil implements WorkflowConstant {

    /**
     * 判断是否为最后一个元素
     * 实体类需要重写equals()方法
     * @param list
     * @param target
     * @return
     * @param <T>
     */
    public static <T> boolean isLastElement(List<T> list, T target) {
        if (list == null || list.isEmpty()) return false;
        T lastElement = list.get(list.size() - 1);
        return Objects.equals(lastElement, target); // 调用 equals 方法
    }

    public static void resetSortOrder(List<WfTaskParticipant> wfTaskParticipantListt){
        for (int i = 0;i < wfTaskParticipantListt.size();i++) {
            WfTaskParticipant wfTaskParticipant = wfTaskParticipantListt.get(i);
            wfTaskParticipant.setSortOrder((i + 1) * FLOW_SORT_ORDER_CONSTANT);
        }
    }

}
