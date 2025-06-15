package com.epiroc.workflow.common.system.flow;

import java.util.List;
import java.util.Map;

/**
 * 用作装饰器里的 Component
 * 定义一个对象接口，可以给这些对象动态地添加职责
 * @author Theo Zheng
 */
public interface IFlow<T> {

    public List flow(List flowList, T param);

    public Map<String, Object> flowInfo(Map<String, Object> resultMap, T param);

}
