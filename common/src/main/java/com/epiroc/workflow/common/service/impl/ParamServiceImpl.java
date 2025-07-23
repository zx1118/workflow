package com.epiroc.workflow.common.service.impl;

import com.epiroc.workflow.common.service.WorkflowDynamicService;
import com.epiroc.workflow.common.util.DateUtils;
import com.epiroc.workflow.common.util.oConvertUtils;
import com.epiroc.workflow.common.service.ParamService;
import com.epiroc.workflow.common.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ParamServiceImpl implements ParamService {

    @Autowired
    private WorkflowDynamicService workflowDynamicService;

    @Autowired
    private TestService testService;

    /**
     * 插入参数
     *
     * @param className 类名
     * @param param     参数
     * @param paramList 参数列表
     * @param idFieldName id字段名
     * @return
     */
    @Override
    public String insertParam(String className, Map<String, Object> param, List<Map<String, Object>> paramList, String idFieldName) {
        String id = null;
        if (oConvertUtils.isNotEmpty(param)) {
            id = workflowDynamicService.insertWithStringId(className, param);
        }

        // 将ID设置到Map列表的指定key中
        if (oConvertUtils.isNotEmpty(idFieldName) && oConvertUtils.isNotEmpty(id) && paramList != null && paramList.size() > 0) {
            for (Map<String, Object> map : paramList) {
                map.put(idFieldName, id);
                workflowDynamicService.insert(className, map);
            }
        }
        return id;
    }

    @Override
    public <T> String insertParam(String className, T param) {
        String id = null;
        if (oConvertUtils.isNotEmpty(param)) {
            id = workflowDynamicService.insertWithStringId(className, param);
        }
        return id;
    }

    @Override
    public <T> String insertParam(T param) {
        String id = null;
        if (oConvertUtils.isNotEmpty(param)) {
            id = workflowDynamicService.insertWithStringId(param);
        }
        return id;
    }

    /**
     * 更新参数
     *
     * @param className 类名
     * @param param     参数
     * @param paramList 参数列表
     * @param idFieldName id字段名
     * @return
     */
    @Override
    public boolean updateParam(String className, Map<String, Object> param, List<Map<String, Object>> paramList, String idFieldName) {
        boolean success = false;
        if (oConvertUtils.isNotEmpty(param)) {
            param.put("updateTime", DateUtils.getDate());
            success = workflowDynamicService.update(className, param);
        }

        // 获取主实体的ID并更新Map列表
        if (oConvertUtils.isNotEmpty(idFieldName) && param != null && param.containsKey("id") && paramList != null && paramList.size() > 0) {
            Object id = param.get("id");
            for (Map<String, Object> map : paramList) {
                map.put(idFieldName, id);
                workflowDynamicService.update(className, map);
            }
        }
        return success;
    }

    @Override
    public <T> boolean updateParam(String className, T param) {
        boolean success = false;
        if (oConvertUtils.isNotEmpty(param)) {
            success = workflowDynamicService.update(className, param);
        }
        return success;
    }

    @Override
    public <T> boolean updateParam(T param) {
        boolean success = false;
        if (oConvertUtils.isNotEmpty(param)) {
            success = workflowDynamicService.update(param);
        }
        return success;
    }

    /**
     * 插入主实体并将其ID设置到子实体列表的指定字段中
     * @param param 主实体对象
     * @param paramList 子实体列表
     * @param idFieldName 子实体中用于存储主实体ID的字段名
     * @return 主实体插入后的ID
     */
    public <T> String insertParamAndSetIdToChildListSameType(T param, List<T> paramList, String idFieldName) {
        String id = null;
        if (oConvertUtils.isNotEmpty(param)) {
            // 插入主实体并获取ID
            id = workflowDynamicService.insertWithStringId(param);
            
            // 将主实体ID设置到子实体列表的指定字段中
            if (oConvertUtils.isNotEmpty(id) && paramList != null && paramList.size() > 0) {
                for (T entity : paramList) {
                    try {
                        // 使用反射设置字段值
                        setFieldValue(entity, idFieldName, id);
                    } catch (Exception e) {
                        throw new RuntimeException("设置字段 " + idFieldName + " 失败: " + e.getMessage(), e);
                    }
                }
                
                // 批量插入子实体
                workflowDynamicService.insertBatch(paramList);
            }
        }
        return id;
    }

    /**
     * 插入主实体并将其ID设置到子实体列表的指定字段中（支持不同类型的主实体和子实体）
     * @param param 主实体对象
     * @param paramList 子实体列表
     * @param idFieldName 子实体中用于存储主实体ID的字段名
     * @return 主实体插入后的ID
     */
    public <T, U> String insertParamAndSetIdToChildListDifferentType(T param, List<U> paramList, String idFieldName) {
        String id = null;
        if (oConvertUtils.isNotEmpty(param)) {
            // 插入主实体并获取ID
            id = workflowDynamicService.insertWithStringId(param);
            
            // 将主实体ID设置到子实体列表的指定字段中
            if (oConvertUtils.isNotEmpty(id) && paramList != null && paramList.size() > 0) {
                for (U entity : paramList) {
                    try {
                        // 使用反射设置字段值
                        setFieldValue(entity, idFieldName, id);
                    } catch (Exception e) {
                        throw new RuntimeException("设置字段 " + idFieldName + " 失败: " + e.getMessage(), e);
                    }
                }
                
                // 批量插入子实体
                workflowDynamicService.insertBatch(paramList);
            }
        }
        return id;
    }

    /**
     * 插入主实体并将其Long类型ID设置到子实体列表的指定字段中
     * @param param 主实体对象
     * @param paramList 子实体列表
     * @param idFieldName 子实体中用于存储主实体ID的字段名
     * @return 主实体插入后的Long类型ID
     */
    public <T, U> Long insertParamAndSetLongIdToChildList(T param, List<U> paramList, String idFieldName) {
        Long id = null;
        if (oConvertUtils.isNotEmpty(param)) {
            // 插入主实体并获取Long类型ID
            id = workflowDynamicService.insertWithLongId(param);
            
            // 将主实体ID设置到子实体列表的指定字段中
            if (id != null && paramList != null && paramList.size() > 0) {
                for (U entity : paramList) {
                    try {
                        // 使用反射设置字段值
                        setFieldValue(entity, idFieldName, id);
                    } catch (Exception e) {
                        throw new RuntimeException("设置字段 " + idFieldName + " 失败: " + e.getMessage(), e);
                    }
                }
                
                // 批量插入子实体
                workflowDynamicService.insertBatch(paramList);
            }
        }
        return id;
    }

    /**
     * 使用反射设置对象字段值
     * @param obj 目标对象
     * @param fieldName 字段名
     * @param value 字段值
     */
    private void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Class<?> clazz = obj.getClass();
        java.lang.reflect.Field field = null;

        // 尝试直接获取字段
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // 如果直接获取失败，尝试获取父类字段
            Class<?> superClass = clazz.getSuperclass();
            while (superClass != null) {
                try {
                    field = superClass.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException ex) {
                    superClass = superClass.getSuperclass();
                }
            }
        }

        if (field != null) {
            field.setAccessible(true);
            field.set(obj, value);
        } else {
            throw new NoSuchFieldException("未找到字段: " + fieldName + " 在类 " + clazz.getName());
        }
    }

    /**
     * 插入主实体并将其ID设置到Map列表的指定key中，然后批量插入Map列表
     * @param param 主实体对象
     * @param mapList Map列表
     * @param className Map对应的实体类名
     * @param idKeyName Map中用于存储主实体ID的key名
     * @return 主实体插入后的ID
     */
    public <T> String insertParamAndSetIdToMapList(T param, List<Map<String, Object>> mapList, String className, String idKeyName) {
        String id = null;
        if (oConvertUtils.isNotEmpty(param)) {
            // 插入主实体并获取ID
            id = workflowDynamicService.insertWithStringId(param);
            
            // 将主实体ID设置到Map列表的指定key中
            if (oConvertUtils.isNotEmpty(id) && mapList != null && mapList.size() > 0) {
                for (Map<String, Object> map : mapList) {
                    map.put(idKeyName, id);
                }
                
                // 批量插入Map列表
                workflowDynamicService.insertBatchMaps(className, mapList);
            }
        }
        return id;
    }

    /**
     * 插入主实体并将其Long类型ID设置到Map列表的指定key中，然后批量插入Map列表
     * @param param 主实体对象
     * @param mapList Map列表
     * @param className Map对应的实体类名
     * @param idKeyName Map中用于存储主实体ID的key名
     * @return 主实体插入后的Long类型ID
     */
    public <T> Long insertParamAndSetLongIdToMapList(T param, List<Map<String, Object>> mapList, String className, String idKeyName) {
        Long id = null;
        if (oConvertUtils.isNotEmpty(param)) {
            // 插入主实体并获取Long类型ID
            id = workflowDynamicService.insertWithLongId(param);
            
            // 将主实体ID设置到Map列表的指定key中
            if (id != null && mapList != null && mapList.size() > 0) {
                for (Map<String, Object> map : mapList) {
                    map.put(idKeyName, id);
                }
                
                // 批量插入Map列表
                workflowDynamicService.insertBatchMaps(className, mapList);
            }
        }
        return id;
    }

    /**
     * 插入主实体并将其ID设置到Map列表的指定key中，然后批量插入Map列表（通过Class类型）
     * @param param 主实体对象
     * @param mapList Map列表
     * @param clazz Map对应的实体类Class
     * @param idKeyName Map中用于存储主实体ID的key名
     * @return 主实体插入后的ID
     */
    public <T> String insertParamAndSetIdToMapList(T param, List<Map<String, Object>> mapList, Class<?> clazz, String idKeyName) {
        return insertParamAndSetIdToMapList(param, mapList, clazz.getName(), idKeyName);
    }

    /**
     * 插入主实体并将其Long类型ID设置到Map列表的指定key中，然后批量插入Map列表（通过Class类型）
     * @param param 主实体对象
     * @param mapList Map列表
     * @param clazz Map对应的实体类Class
     * @param idKeyName Map中用于存储主实体ID的key名
     * @return 主实体插入后的Long类型ID
     */
    public <T> Long insertParamAndSetLongIdToMapList(T param, List<Map<String, Object>> mapList, Class<?> clazz, String idKeyName) {
        return insertParamAndSetLongIdToMapList(param, mapList, clazz.getName(), idKeyName);
    }

    /**
     * 插入主Map并将其ID设置到Map列表的指定key中，然后批量插入Map列表
     * @param mainClassName 主Map对应的实体类名
     * @param mainMap 主Map数据
     * @param childMapList 子Map列表
     * @param childClassName 子Map对应的实体类名
     * @param idKeyName 子Map中用于存储主实体ID的key名
     * @return 主实体插入后的ID
     */
    public String insertMainMapAndSetIdToChildMapList(String mainClassName, Map<String, Object> mainMap, 
                                                     List<Map<String, Object>> childMapList, String childClassName, String idKeyName) {
        String id = null;
        if (oConvertUtils.isNotEmpty(mainMap)) {
            // 插入主Map并获取ID
            id = workflowDynamicService.insertWithStringId(mainClassName, mainMap);
            
            // 将主Map的ID设置到子Map列表的指定key中
            if (oConvertUtils.isNotEmpty(id) && childMapList != null && childMapList.size() > 0) {
                for (Map<String, Object> childMap : childMapList) {
                    childMap.put(idKeyName, id);
                }
                
                // 批量插入子Map列表
                workflowDynamicService.insertBatchMaps(childClassName, childMapList);
            }
        }
        return id;
    }

    /**
     * 插入主Map并将其Long类型ID设置到Map列表的指定key中，然后批量插入Map列表
     * @param mainClassName 主Map对应的实体类名
     * @param mainMap 主Map数据
     * @param childMapList 子Map列表
     * @param childClassName 子Map对应的实体类名
     * @param idKeyName 子Map中用于存储主实体ID的key名
     * @return 主实体插入后的Long类型ID
     */
    public Long insertMainMapAndSetLongIdToChildMapList(String mainClassName, Map<String, Object> mainMap, 
                                                       List<Map<String, Object>> childMapList, String childClassName, String idKeyName) {
        Long id = null;
        if (oConvertUtils.isNotEmpty(mainMap)) {
            // 插入主Map并获取Long类型ID
            id = workflowDynamicService.insertWithLongId(mainClassName, mainMap);
            
            // 将主Map的ID设置到子Map列表的指定key中
            if (id != null && childMapList != null && childMapList.size() > 0) {
                for (Map<String, Object> childMap : childMapList) {
                    childMap.put(idKeyName, id);
                }
                
                // 批量插入子Map列表
                workflowDynamicService.insertBatchMaps(childClassName, childMapList);
            }
        }
        return id;
    }

    /**
     * 插入主Map并将其ID设置到Map列表的指定key中，然后批量插入Map列表（通过Class类型）
     * @param mainClass 主Map对应的实体类Class
     * @param mainMap 主Map数据
     * @param childMapList 子Map列表
     * @param childClass 子Map对应的实体类Class
     * @param idKeyName 子Map中用于存储主实体ID的key名
     * @return 主实体插入后的ID
     */
    public String insertMainMapAndSetIdToChildMapList(Class<?> mainClass, Map<String, Object> mainMap, 
                                                     List<Map<String, Object>> childMapList, Class<?> childClass, String idKeyName) {
        return insertMainMapAndSetIdToChildMapList(mainClass.getName(), mainMap, childMapList, childClass.getName(), idKeyName);
    }

    /**
     * 插入主Map并将其Long类型ID设置到Map列表的指定key中，然后批量插入Map列表（通过Class类型）
     * @param mainClass 主Map对应的实体类Class
     * @param mainMap 主Map数据
     * @param childMapList 子Map列表
     * @param childClass 子Map对应的实体类Class
     * @param idKeyName 子Map中用于存储主实体ID的key名
     * @return 主实体插入后的Long类型ID
     */
    public Long insertMainMapAndSetLongIdToChildMapList(Class<?> mainClass, Map<String, Object> mainMap, 
                                                       List<Map<String, Object>> childMapList, Class<?> childClass, String idKeyName) {
        return insertMainMapAndSetLongIdToChildMapList(mainClass.getName(), mainMap, childMapList, childClass.getName(), idKeyName);
    }

    /**
     * 更新主实体并将其ID设置到子实体列表的指定字段中
     * @param param 主实体对象
     * @param paramList 子实体列表
     * @param idFieldName 子实体中用于存储主实体ID的字段名
     * @return 更新是否成功
     */
    @Override
    public <T> boolean updateParamAndSetIdToChildListSameType(T param, List<T> paramList, String idFieldName) {
        boolean success = false;
        if (oConvertUtils.isNotEmpty(param)) {
            // 更新主实体
            success = workflowDynamicService.update(param);
            
            // 获取主实体ID并设置到子实体列表的指定字段中
            if (success && paramList != null && paramList.size() > 0) {
                try {
                    Object id = getFieldValue(param, "id");
                    if (id != null) {
                        for (T entity : paramList) {
                            // 使用反射设置字段值
                            setFieldValue(entity, idFieldName, id);
                        }
                        
                        // 批量更新子实体
                        workflowDynamicService.updateBatch(paramList);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("更新子实体失败: " + e.getMessage(), e);
                }
            }
        }
        return success;
    }

    /**
     * 更新主实体并将其ID设置到子实体列表的指定字段中（支持不同类型的主实体和子实体）
     * @param param 主实体对象
     * @param paramList 子实体列表
     * @param idFieldName 子实体中用于存储主实体ID的字段名
     * @return 更新是否成功
     */
    @Override
    public <T, U> boolean updateParamAndSetIdToChildListDifferentType(T param, List<U> paramList, String idFieldName) {
        boolean success = false;
        if (oConvertUtils.isNotEmpty(param)) {
            // 更新主实体
            success = workflowDynamicService.update(param);
            
            // 获取主实体ID并设置到子实体列表的指定字段中
            if (success && paramList != null && paramList.size() > 0) {
                try {
                    Object id = getFieldValue(param, "id");
                    if (id != null) {
                        for (U entity : paramList) {
                            // 使用反射设置字段值
                            setFieldValue(entity, idFieldName, id);
                        }
                        
                        // 批量更新子实体
                        workflowDynamicService.updateBatch(paramList);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("更新子实体失败: " + e.getMessage(), e);
                }
            }
        }
        return success;
    }

    /**
     * 更新主实体并将其Long类型ID设置到子实体列表的指定字段中
     * @param param 主实体对象
     * @param paramList 子实体列表
     * @param idFieldName 子实体中用于存储主实体ID的字段名
     * @return 更新是否成功
     */
    @Override
    public <T, U> boolean updateParamAndSetLongIdToChildList(T param, List<U> paramList, String idFieldName) {
        boolean success = false;
        if (oConvertUtils.isNotEmpty(param)) {
            // 更新主实体
            success = workflowDynamicService.update(param);
            
            // 获取主实体ID并设置到子实体列表的指定字段中
            if (success && paramList != null && paramList.size() > 0) {
                try {
                    Object id = getFieldValue(param, "id");
                    if (id != null) {
                        for (U entity : paramList) {
                            // 使用反射设置字段值
                            setFieldValue(entity, idFieldName, id);
                        }
                        
                        // 批量更新子实体
                        workflowDynamicService.updateBatch(paramList);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("更新子实体失败: " + e.getMessage(), e);
                }
            }
        }
        return success;
    }

    /**
     * 更新主实体并将其ID设置到Map列表的指定key中，然后批量更新Map列表
     * @param param 主实体对象
     * @param mapList Map列表
     * @param className Map对应的实体类名
     * @param idKeyName Map中用于存储主实体ID的key名
     * @return 更新是否成功
     */
    @Override
    public <T> boolean updateParamAndSetIdToMapList(T param, List<Map<String, Object>> mapList, String className, String idKeyName) {
        boolean success = false;
        if (oConvertUtils.isNotEmpty(param)) {
            // 更新主实体
            success = workflowDynamicService.update(param);
            
            // 获取主实体ID并设置到Map列表的指定key中
            if (success && mapList != null && mapList.size() > 0) {
                try {
                    Object id = getFieldValue(param, "id");
                    if (id != null) {
                        for (Map<String, Object> map : mapList) {
                            map.put(idKeyName, id);
                        }
                        
                        // 批量更新Map列表
                        workflowDynamicService.updateBatchMaps(className, mapList);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("更新Map列表失败: " + e.getMessage(), e);
                }
            }
        }
        return success;
    }

    /**
     * 更新主实体并将其Long类型ID设置到Map列表的指定key中，然后批量更新Map列表
     * @param param 主实体对象
     * @param mapList Map列表
     * @param className Map对应的实体类名
     * @param idKeyName Map中用于存储主实体ID的key名
     * @return 更新是否成功
     */
    @Override
    public <T> boolean updateParamAndSetLongIdToMapList(T param, List<Map<String, Object>> mapList, String className, String idKeyName) {
        boolean success = false;
        if (oConvertUtils.isNotEmpty(param)) {
            // 更新主实体
            success = workflowDynamicService.update(param);
            
            // 获取主实体ID并设置到Map列表的指定key中
            if (success && mapList != null && mapList.size() > 0) {
                try {
                    Object id = getFieldValue(param, "id");
                    if (id != null) {
                        for (Map<String, Object> map : mapList) {
                            map.put(idKeyName, id);
                        }
                        
                        // 批量更新Map列表
                        workflowDynamicService.updateBatchMaps(className, mapList);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("更新Map列表失败: " + e.getMessage(), e);
                }
            }
        }
        return success;
    }

    /**
     * 更新主实体并将其ID设置到Map列表的指定key中，然后批量更新Map列表（通过Class类型）
     * @param param 主实体对象
     * @param mapList Map列表
     * @param clazz Map对应的实体类Class
     * @param idKeyName Map中用于存储主实体ID的key名
     * @return 更新是否成功
     */
    @Override
    public <T> boolean updateParamAndSetIdToMapList(T param, List<Map<String, Object>> mapList, Class<?> clazz, String idKeyName) {
        return updateParamAndSetIdToMapList(param, mapList, clazz.getName(), idKeyName);
    }

    /**
     * 更新主实体并将其Long类型ID设置到Map列表的指定key中，然后批量更新Map列表（通过Class类型）
     * @param param 主实体对象
     * @param mapList Map列表
     * @param clazz Map对应的实体类Class
     * @param idKeyName Map中用于存储主实体ID的key名
     * @return 更新是否成功
     */
    @Override
    public <T> boolean updateParamAndSetLongIdToMapList(T param, List<Map<String, Object>> mapList, Class<?> clazz, String idKeyName) {
        return updateParamAndSetLongIdToMapList(param, mapList, clazz.getName(), idKeyName);
    }

    /**
     * 更新主Map并将其ID设置到Map列表的指定key中，然后批量更新Map列表
     * @param mainClassName 主Map对应的实体类名
     * @param mainMap 主Map数据
     * @param childMapList 子Map列表
     * @param childClassName 子Map对应的实体类名
     * @param idKeyName 子Map中用于存储主实体ID的key名
     * @return 更新是否成功
     */
    @Override
    public boolean updateMainMapAndSetIdToChildMapList(String mainClassName, Map<String, Object> mainMap, 
                                                      List<Map<String, Object>> childMapList, String childClassName, String idKeyName) {
        boolean success = false;
        if (oConvertUtils.isNotEmpty(mainMap)) {
            // 更新主Map
            success = workflowDynamicService.update(mainClassName, mainMap);
            
            // 获取主Map的ID并设置到子Map列表的指定key中
            if (success && childMapList != null && childMapList.size() > 0) {
                Object id = mainMap.get("id");
                if (id != null) {
                    for (Map<String, Object> childMap : childMapList) {
                        childMap.put(idKeyName, id);
                    }
                    
                    // 批量更新子Map列表
                    workflowDynamicService.updateBatchMaps(childClassName, childMapList);
                }
            }
        }
        return success;
    }

    /**
     * 更新主Map并将其Long类型ID设置到Map列表的指定key中，然后批量更新Map列表
     * @param mainClassName 主Map对应的实体类名
     * @param mainMap 主Map数据
     * @param childMapList 子Map列表
     * @param childClassName 子Map对应的实体类名
     * @param idKeyName 子Map中用于存储主实体ID的key名
     * @return 更新是否成功
     */
    @Override
    public boolean updateMainMapAndSetLongIdToChildMapList(String mainClassName, Map<String, Object> mainMap, 
                                                          List<Map<String, Object>> childMapList, String childClassName, String idKeyName) {
        boolean success = false;
        if (oConvertUtils.isNotEmpty(mainMap)) {
            // 更新主Map
            success = workflowDynamicService.update(mainClassName, mainMap);
            
            // 获取主Map的ID并设置到子Map列表的指定key中
            if (success && childMapList != null && childMapList.size() > 0) {
                Object id = mainMap.get("id");
                if (id != null) {
                    for (Map<String, Object> childMap : childMapList) {
                        childMap.put(idKeyName, id);
                    }
                    
                    // 批量更新子Map列表
                    workflowDynamicService.updateBatchMaps(childClassName, childMapList);
                }
            }
        }
        return success;
    }

    /**
     * 更新主Map并将其ID设置到Map列表的指定key中，然后批量更新Map列表（通过Class类型）
     * @param mainClass 主Map对应的实体类Class
     * @param mainMap 主Map数据
     * @param childMapList 子Map列表
     * @param childClass 子Map对应的实体类Class
     * @param idKeyName 子Map中用于存储主实体ID的key名
     * @return 更新是否成功
     */
    @Override
    public boolean updateMainMapAndSetIdToChildMapList(Class<?> mainClass, Map<String, Object> mainMap, 
                                                      List<Map<String, Object>> childMapList, Class<?> childClass, String idKeyName) {
        return updateMainMapAndSetIdToChildMapList(mainClass.getName(), mainMap, childMapList, childClass.getName(), idKeyName);
    }

    /**
     * 更新主Map并将其Long类型ID设置到Map列表的指定key中，然后批量更新Map列表（通过Class类型）
     * @param mainClass 主Map对应的实体类Class
     * @param mainMap 主Map数据
     * @param childMapList 子Map列表
     * @param childClass 子Map对应的实体类Class
     * @param idKeyName 子Map中用于存储主实体ID的key名
     * @return 更新是否成功
     */
    @Override
    public boolean updateMainMapAndSetLongIdToChildMapList(Class<?> mainClass, Map<String, Object> mainMap, 
                                                          List<Map<String, Object>> childMapList, Class<?> childClass, String idKeyName) {
        return updateMainMapAndSetLongIdToChildMapList(mainClass.getName(), mainMap, childMapList, childClass.getName(), idKeyName);
    }

    /**
     * 使用反射获取对象字段值
     * @param obj 目标对象
     * @param fieldName 字段名
     * @return 字段值
     */
    private Object getFieldValue(Object obj, String fieldName) throws Exception {
        Class<?> clazz = obj.getClass();
        java.lang.reflect.Field field = null;

        // 尝试直接获取字段
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // 如果直接获取失败，尝试获取父类字段
            Class<?> superClass = clazz.getSuperclass();
            while (superClass != null) {
                try {
                    field = superClass.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException ex) {
                    superClass = superClass.getSuperclass();
                }
            }
        }

        if (field != null) {
            field.setAccessible(true);
            return field.get(obj);
        } else {
            throw new NoSuchFieldException("未找到字段: " + fieldName + " 在类 " + clazz.getName());
        }
    }

}
