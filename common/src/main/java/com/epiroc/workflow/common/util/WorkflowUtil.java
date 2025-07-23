package com.epiroc.workflow.common.util;

import com.epiroc.workflow.common.entity.WfTaskParticipant;
import com.epiroc.workflow.common.system.constant.WorkflowConstant;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 工作流工具类
 * 
 * 提供工作流相关的通用工具方法，包括：
 * 1. 列表操作工具方法
 * 2. 动态SQL查询条件生成
 * 3. 实体字段过滤
 * 
 * 新增方法说明：
 * 
 * 1. generateSqlQuery() - 动态SQL查询条件生成
 *    - 根据传入的Map参数生成SQL查询语句
 *    - 支持驼峰和下划线字段名自动转换
 *    - 支持等于(=)、模糊(like)和包含(in)三种查询方式
 *    - 当值为List类型时自动使用IN查询
 *    - 支持表别名动态拼接
 *    - 内置SQL注入防护
 * 
 * 2. filterEntityFields() - 实体字段过滤
 *    - 根据实体类名过滤Map中的字段
 *    - 只保留实体类中存在的字段，过滤无效字段
 *    - 支持驼峰和下划线字段名格式
 *    - 提供安全的数据过滤，防止无效参数传入数据库操作
 * 
 * 使用示例：
 * <pre>
 * // 1. 先过滤有效字段
 * Map&lt;String, Object&gt; frontendParams = ...; // 前端传入的参数
 * Map&lt;String, Object&gt; validParams = WorkflowUtil.filterEntityFields(
 *     "com.epiroc.workflow.common.entity.ScParam", frontendParams);
 * 
 * // 2. 再生成SQL查询条件
 * String sql = WorkflowUtil.generateSqlQuery(validParams, "sp");
 * 
 * // 3. 在Mapper中使用
 * String fullSql = "SELECT * FROM sc_param sp WHERE " + sql;
 * </pre>
 * 
 * @author Workflow Team
 * @version 1.0
 */
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

    /**
     * 根据传入的Map参数生成SQL查询语句
     * 支持驼峰和下划线字段名转换，支持=、like和in查询方式
     * 
     * @param queryParams 查询参数Map，key为字段名，value为查询值（支持String、List等类型）
     * @param tableAlias 数据表别名，如"wpd"
     * @return 生成的SQL查询条件字符串，可用于mapper查询
     * 
     * 示例用法：
     * Map<String, Object> params = new HashMap<>();
     * params.put("costCenter", "test");
     * params.put("userName", "张三");
     * params.put("userIds", Arrays.asList("001", "002", "003"));  // List类型使用IN查询
     * String sql = generateSqlQuery(params, "wpd");
     * 结果：(wpd.cost_center = 'test' or wpd.cost_center like concat('%','test','%')) and (wpd.user_name = '张三' or wpd.user_name like concat('%','张三','%')) and wpd.user_ids in ('001','002','003')
     */
    public static String generateSqlQuery(Map<String, Object> queryParams, String tableAlias) {
        return generateSqlQuery(queryParams, tableAlias, QueryMode.BOTH);
    }
    
    /**
     * 根据传入的Map参数生成SQL查询语句（支持指定查询模式）
     * 支持驼峰和下划线字段名转换，支持=、like和in查询方式
     * 
     * @param queryParams 查询参数Map，key为字段名，value为查询值（支持String、List等类型）
     * @param tableAlias 数据表别名，如"wpd"
     * @param queryMode 查询模式：EQUAL(只等于)、LIKE(只模糊)、BOTH(两者都有)
     * @return 生成的SQL查询条件字符串，可用于mapper查询
     * 
     * 示例用法：
     * Map<String, Object> params = new HashMap<>();
     * params.put("costCenter", "test");
     * params.put("userIds", Arrays.asList("001", "002", "003"));  // List类型使用IN查询
     * String sql = generateSqlQuery(params, "wpd", QueryMode.EQUAL);
     * 结果：wpd.cost_center = 'test' and wpd.user_ids in ('001','002','003')
     */
    public static String generateSqlQuery(Map<String, Object> queryParams, String tableAlias, QueryMode queryMode) {
        if (queryParams == null || queryParams.isEmpty()) {
            return "";
        }
        
        StringBuilder sqlBuilder = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            
            // 跳过空值
            if (value == null || (value instanceof String && ((String) value).trim().isEmpty()) 
                || (value instanceof List && ((List<?>) value).isEmpty())) {
                continue;
            }
            
            // 转换字段名：驼峰转下划线
            String columnName = convertFieldNameToColumn(fieldName);
            
            // 拼接表别名
            String fullColumnName = (tableAlias != null && !tableAlias.trim().isEmpty()) 
                    ? tableAlias + "." + columnName 
                    : columnName;
            
            // 添加连接符
            if (!first) {
                sqlBuilder.append(" and ");
            }
            first = false;
            
            // 处理List类型：使用IN查询
            if (value instanceof List) {
                List<?> listValue = (List<?>) value;
                sqlBuilder.append(fullColumnName).append(" in (");
                
                boolean firstItem = true;
                for (Object item : listValue) {
                    if (item != null) {
                        if (!firstItem) {
                            sqlBuilder.append(",");
                        }
                        firstItem = false;
                        
                        String safeItem = sanitizeValue(item.toString());
                        sqlBuilder.append("'").append(safeItem).append("'");
                    }
                }
                sqlBuilder.append(")");
            } else {
                // 处理单个值：根据查询模式生成条件
                String safeValue = sanitizeValue(value.toString());
                
                switch (queryMode) {
                    case EQUAL:
                        sqlBuilder.append(fullColumnName).append(" = '").append(safeValue).append("'");
                        break;
                    case LIKE:
                        sqlBuilder.append(fullColumnName).append(" like concat('%','").append(safeValue).append("','%')");
                        break;
                    case BOTH:
                    default:
                        sqlBuilder.append("(")
                                 .append(fullColumnName).append(" = '").append(safeValue).append("'")
                                 .append(" or ")
                                 .append(fullColumnName).append(" like concat('%','").append(safeValue).append("','%')")
                                 .append(")");
                        break;
                }
            }
        }
        
        return sqlBuilder.toString();
    }
    
    /**
     * 查询模式枚举
     */
    public enum QueryMode {
        /** 只使用等于查询 */
        EQUAL,
        /** 只使用模糊查询 */
        LIKE,
        /** 同时使用等于和模糊查询 */
        BOTH
    }
    
    /**
     * 将字段名转换为数据库列名（驼峰转下划线）
     * 
     * @param fieldName 字段名（可能是驼峰或下划线格式）
     * @return 下划线格式的列名
     */
    private static String convertFieldNameToColumn(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }
        
        // 如果已经是下划线格式，直接返回
        if (fieldName.contains("_")) {
            return fieldName.toLowerCase();
        }
        
        // 驼峰转下划线
        return oConvertUtils.camelToUnderline(fieldName);
    }
    
    /**
     * 清理查询值，防止SQL注入
     * 
     * @param value 原始值
     * @return 清理后的安全值
     */
    private static String sanitizeValue(String value) {
        if (value == null) {
            return "";
        }
        
        // 移除或转义危险字符
        return value.replace("'", "''")  // 转义单引号
                   .replace("\\", "\\\\") // 转义反斜杠
                   .replace("\"", "\\\"") // 转义双引号
                   .replace(";", "")      // 移除分号
                   .replace("--", "")     // 移除SQL注释
                   .replace("/*", "")     // 移除SQL块注释开始
                   .replace("*/", "")     // 移除SQL块注释结束
                   .trim();
    }

    /**
     * 根据实体类名过滤Map中的字段，只保留实体类中存在的字段
     * 
     * @param entityClassName 实体类全名，如"org.jeecg.modules.tne.entity.param.TneVatCheckParam"
     * @param paramMap 原始参数Map
     * @return 过滤后的Map，只包含实体类中存在的字段
     * 
     * 示例用法：
     * Map<String, Object> params = new HashMap<>();
     * params.put("userName", "张三");
     * params.put("invalidField", "test");  // 实体类中不存在的字段
     * params.put("costCenter", "IT");
     * 
     * Map<String, Object> filteredParams = filterEntityFields(
     *     "com.epiroc.workflow.common.entity.ScParam", params);
     * // 结果只包含实体类中存在的字段
     */
    public static Map<String, Object> filterEntityFields(String entityClassName, Map<String, Object> paramMap) {
        Map<String, Object> filteredMap = new HashMap<>();
        
        if (paramMap == null || paramMap.isEmpty()) {
            return filteredMap;
        }
        
        if (entityClassName == null || entityClassName.trim().isEmpty()) {
            throw new IllegalArgumentException("实体类名不能为空");
        }
        
        try {
            // 获取实体类
            Class<?> entityClass = Class.forName(entityClassName);
            
            // 获取实体类的所有字段（包括父类字段）
            List<Field> allFields = ReflectHelper.getClassFields(entityClass);
            
            // 遍历原始Map，检查每个字段是否在实体类中存在
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();
                
                // 检查字段是否存在于实体类中（支持驼峰和下划线两种格式）
                if (isFieldExistInEntity(fieldName, allFields)) {
                    filteredMap.put(fieldName, value);
                }
            }
            
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("找不到指定的实体类: " + entityClassName, e);
        } catch (Exception e) {
            throw new RuntimeException("过滤实体字段时发生错误", e);
        }
        
        return filteredMap;
    }
    
    /**
     * 根据实体类过滤Map中的字段（直接传入Class对象）
     * 
     * @param entityClass 实体类Class对象
     * @param paramMap 原始参数Map
     * @return 过滤后的Map，只包含实体类中存在的字段
     */
    public static Map<String, Object> filterEntityFields(Class<?> entityClass, Map<String, Object> paramMap) {
        Map<String, Object> filteredMap = new HashMap<>();
        
        if (paramMap == null || paramMap.isEmpty()) {
            return filteredMap;
        }
        
        if (entityClass == null) {
            throw new IllegalArgumentException("实体类不能为空");
        }
        
        try {
            // 获取实体类的所有字段（包括父类字段）
            List<Field> allFields = ReflectHelper.getClassFields(entityClass);
            
            // 遍历原始Map，检查每个字段是否在实体类中存在
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();
                
                // 检查字段是否存在于实体类中（支持驼峰和下划线两种格式）
                if (isFieldExistInEntity(fieldName, allFields)) {
                    filteredMap.put(fieldName, value);
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("过滤实体字段时发生错误", e);
        }
        
        return filteredMap;
    }
    
    /**
     * 检查字段是否存在于实体类中
     * 支持驼峰命名和下划线命名的字段匹配
     * 
     * @param fieldName 要检查的字段名
     * @param entityFields 实体类的所有字段列表
     * @return 是否存在
     */
    private static boolean isFieldExistInEntity(String fieldName, List<Field> entityFields) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            return false;
        }
        
        String trimmedFieldName = fieldName.trim();
        
        for (Field field : entityFields) {
            String entityFieldName = field.getName();
            
            // 直接匹配字段名（驼峰格式）
            if (entityFieldName.equals(trimmedFieldName)) {
                return true;
            }
            
            // 将实体字段名转换为下划线格式进行匹配
            String underlineFieldName = oConvertUtils.camelToUnderline(entityFieldName);
            if (underlineFieldName.equals(trimmedFieldName)) {
                return true;
            }
            
            // 将输入字段名转换为驼峰格式进行匹配（如果输入是下划线格式）
            if (trimmedFieldName.contains("_")) {
                String camelFieldName = oConvertUtils.camelName(trimmedFieldName);
                if (entityFieldName.equals(camelFieldName)) {
                    return true;
                }
            }
        }
        
        return false;
    }

}
