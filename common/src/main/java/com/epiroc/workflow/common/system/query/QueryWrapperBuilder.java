package com.epiroc.workflow.common.system.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.annotation.TableField;
import com.epiroc.workflow.common.system.annotation.LikeQuery;
import com.epiroc.workflow.common.system.vo.DateRange;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * 根据传入的实体类动态构建查询条件
 */
public class QueryWrapperBuilder {
    public static <T> QueryWrapper<T> buildQueryWrapper(T entity) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        if (entity == null) {
            return queryWrapper;
        }

        Class<?> clazz = entity.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value == null) {
                    continue;
                }

                String columnName = getColumnName(field);
                Class<?> fieldType = field.getType();

                if (fieldType == String.class) {
                    handleStringField(queryWrapper, field, columnName, (String) value);
                } else if (fieldType == Date.class) {
                    handleDateField(queryWrapper, columnName, (Date) value);
                } else {
                    queryWrapper.eq(columnName, value);
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException("反射访问字段失败", e);
            }
        }

        return queryWrapper;
    }

    private static String getColumnName(Field field) {
        TableField tableField = field.getAnnotation(TableField.class);
        return (tableField != null && !tableField.value().isEmpty())
                ? tableField.value()
                : camelToUnderline(field.getName());
    }

    private static void handleStringField(QueryWrapper<?> queryWrapper, Field field,
                                          String columnName, String value) {
        if (field.isAnnotationPresent(LikeQuery.class)) {
            queryWrapper.like(columnName, value);
        } else {
            queryWrapper.eq(columnName, value);
        }
    }

    private static void handleDateField(QueryWrapper<?> queryWrapper,
                                        String columnName, Object value) { // 改为Object类型
        if (value instanceof DateRange) {
            DateRange range = (DateRange) value;
            queryWrapper.between(columnName, range.getStart(), range.getEnd());
        } else if (value instanceof Date) {
            queryWrapper.eq(columnName, value);
        }
    }

    // 驼峰转下划线
    private static String camelToUnderline(String str) {
        return str.replaceAll("([A-Z])", "_$1").toLowerCase();
    }
}
