package com.epiroc.workflow.common.service;

import java.util.List;
import java.util.Map;

public interface ParamService {

    /**
     * 插入参数
     *
     * @param className
     * @param param
     * @param paramList
     * @return
     */
    String insertParam(String className, Map<String, Object> param, List<Map<String, Object>> paramList, String idFieldName);

    <T> String insertParam(String className, T param);

    <T> String insertParam(T param);

    /**
     * 插入主实体并将其ID设置到子实体列表的指定字段中
     * @param param 主实体对象
     * @param paramList 子实体列表
     * @param idFieldName 子实体中用于存储主实体ID的字段名
     * @return 主实体插入后的ID
     */
    <T> String insertParamAndSetIdToChildListSameType(T param, List<T> paramList, String idFieldName);

    /**
     * 插入主实体并将其ID设置到子实体列表的指定字段中（支持不同类型的主实体和子实体）
     * @param param 主实体对象
     * @param paramList 子实体列表
     * @param idFieldName 子实体中用于存储主实体ID的字段名
     * @return 主实体插入后的ID
     */
    <T, U> String insertParamAndSetIdToChildListDifferentType(T param, List<U> paramList, String idFieldName);

    /**
     * 插入主实体并将其Long类型ID设置到子实体列表的指定字段中
     * @param param 主实体对象
     * @param paramList 子实体列表
     * @param idFieldName 子实体中用于存储主实体ID的字段名
     * @return 主实体插入后的Long类型ID
     */
    <T, U> Long insertParamAndSetLongIdToChildList(T param, List<U> paramList, String idFieldName);

    /**
     * 插入主实体并将其ID设置到Map列表的指定key中，然后批量插入Map列表
     * @param param 主实体对象
     * @param mapList Map列表
     * @param className Map对应的实体类名
     * @param idKeyName Map中用于存储主实体ID的key名
     * @return 主实体插入后的ID
     */
    <T> String insertParamAndSetIdToMapList(T param, List<Map<String, Object>> mapList, String className, String idKeyName);

    /**
     * 插入主实体并将其Long类型ID设置到Map列表的指定key中，然后批量插入Map列表
     * @param param 主实体对象
     * @param mapList Map列表
     * @param className Map对应的实体类名
     * @param idKeyName Map中用于存储主实体ID的key名
     * @return 主实体插入后的Long类型ID
     */
    <T> Long insertParamAndSetLongIdToMapList(T param, List<Map<String, Object>> mapList, String className, String idKeyName);

    /**
     * 插入主实体并将其ID设置到Map列表的指定key中，然后批量插入Map列表（通过Class类型）
     * @param param 主实体对象
     * @param mapList Map列表
     * @param clazz Map对应的实体类Class
     * @param idKeyName Map中用于存储主实体ID的key名
     * @return 主实体插入后的ID
     */
    <T> String insertParamAndSetIdToMapList(T param, List<Map<String, Object>> mapList, Class<?> clazz, String idKeyName);

    /**
     * 插入主实体并将其Long类型ID设置到Map列表的指定key中，然后批量插入Map列表（通过Class类型）
     * @param param 主实体对象
     * @param mapList Map列表
     * @param clazz Map对应的实体类Class
     * @param idKeyName Map中用于存储主实体ID的key名
     * @return 主实体插入后的Long类型ID
     */
    <T> Long insertParamAndSetLongIdToMapList(T param, List<Map<String, Object>> mapList, Class<?> clazz, String idKeyName);

    /**
     * 插入主Map并将其ID设置到Map列表的指定key中，然后批量插入Map列表
     * @param mainClassName 主Map对应的实体类名
     * @param mainMap 主Map数据
     * @param childMapList 子Map列表
     * @param childClassName 子Map对应的实体类名
     * @param idKeyName 子Map中用于存储主实体ID的key名
     * @return 主实体插入后的ID
     */
    String insertMainMapAndSetIdToChildMapList(String mainClassName, Map<String, Object> mainMap, 
                                              List<Map<String, Object>> childMapList, String childClassName, String idKeyName);

    /**
     * 插入主Map并将其Long类型ID设置到Map列表的指定key中，然后批量插入Map列表
     * @param mainClassName 主Map对应的实体类名
     * @param mainMap 主Map数据
     * @param childMapList 子Map列表
     * @param childClassName 子Map对应的实体类名
     * @param idKeyName 子Map中用于存储主实体ID的key名
     * @return 主实体插入后的Long类型ID
     */
    Long insertMainMapAndSetLongIdToChildMapList(String mainClassName, Map<String, Object> mainMap, 
                                                List<Map<String, Object>> childMapList, String childClassName, String idKeyName);

    /**
     * 插入主Map并将其ID设置到Map列表的指定key中，然后批量插入Map列表（通过Class类型）
     * @param mainClass 主Map对应的实体类Class
     * @param mainMap 主Map数据
     * @param childMapList 子Map列表
     * @param childClass 子Map对应的实体类Class
     * @param idKeyName 子Map中用于存储主实体ID的key名
     * @return 主实体插入后的ID
     */
    String insertMainMapAndSetIdToChildMapList(Class<?> mainClass, Map<String, Object> mainMap, 
                                              List<Map<String, Object>> childMapList, Class<?> childClass, String idKeyName);

    /**
     * 插入主Map并将其Long类型ID设置到Map列表的指定key中，然后批量插入Map列表（通过Class类型）
     * @param mainClass 主Map对应的实体类Class
     * @param mainMap 主Map数据
     * @param childMapList 子Map列表
     * @param childClass 子Map对应的实体类Class
     * @param idKeyName 子Map中用于存储主实体ID的key名
     * @return 主实体插入后的Long类型ID
     */
    Long insertMainMapAndSetLongIdToChildMapList(Class<?> mainClass, Map<String, Object> mainMap, 
                                                List<Map<String, Object>> childMapList, Class<?> childClass, String idKeyName);

}
