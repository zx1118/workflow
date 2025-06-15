package com.epiroc.workflow.common.system.flow;

import java.util.List;
import java.util.Map;

/**
 * 作为装饰器模式中的Decorator，装饰抽象类
 */
public class FlowSuper<T> implements IFlow<T> {

    protected IFlow component;

    public FlowSuper(IFlow flow) {
        this.component = flow;
    }

    // 装饰对象
    public void decorate(IFlow component) {
        this.component = component;
    }

    @Override
    public List flow(List flowList, T param) {
        List result = null;
        if (this.component != null) {
            // 若装饰对象存在，则调用其flow方法
            result = this.component.flow(flowList, param);
        }
        return result;
    }

    @Override
    public Map<String, Object> flowInfo(Map<String, Object> resultMap, T param) {
        Map<String, Object> result = null;
        if (this.component != null) {
            // 若装饰对象存在，则调用其flow方法
            result = this.component.flowInfo(resultMap, param);
        }
        return result;
    }

}
