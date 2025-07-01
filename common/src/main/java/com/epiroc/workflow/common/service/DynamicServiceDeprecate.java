package com.epiroc.workflow.common.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epiroc.workflow.common.system.exception.DynamicServiceException;
import com.epiroc.workflow.common.system.exception.TypeMismatchException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

@Service
public class DynamicServiceDeprecate {

//    private static final Logger logger = LoggerFactory.getLogger(DynamicServiceDeprecate.class);
//
//    @Autowired
//    private ApplicationContext applicationContext;
//
//    private Map<Class<?>, BaseMapper<?>> entityMapperMap = new HashMap<>();
//
//    // 缓存实体类的主键字段信息（提升反射性能）
//    private static final Map<Class<?>, Field> ID_FIELD_CACHE = new ConcurrentHashMap<>();
//
//    private final Map<Class<?>, Function<Serializable, String>> idConverters = new ConcurrentHashMap<>();
//
//    private final Map<Class<?>, BaseMapper<?>> entityMapperCache = new ConcurrentHashMap<>();
//    private final Map<Class<?>, Field> idFieldCache = new ConcurrentHashMap<>();
//
////    @PostConstruct
////    public void init() {
////        Map<String, BaseMapper> beans = applicationContext.getBeansOfType(BaseMapper.class);
////        for (BaseMapper<?> mapper : beans.values()) {
////            Class<?> entityClass = getEntityClass(mapper);
////            entityMapperMap.put(entityClass, mapper);
////            logger.info("Mapped entity class: {} to mapper: {}", entityClass.getName(), mapper.getClass().getName());
////        }
////    }
//
//    // 初始化阶段加载所有Mapper
//    @PostConstruct
//    public void init() {
//        initializeMapperCache();
//        registerDefaultConverters();
//        setupBeanUtilsDateConverter();
//        logger.info("DynamicService initialized with {} mappers", entityMapperCache.size());
//    }
//
//    private Class<?> getEntityClass(BaseMapper<?> mapper) {
//        // 获取Mapper接口的Class（假设第一个接口是具体的Mapper接口）
//        Class<?> mapperInterface = mapper.getClass().getInterfaces()[0];
//        // 获取Mapper接口的泛型父接口
//        Type[] genericInterfaces = mapperInterface.getGenericInterfaces();
//        for (Type type : genericInterfaces) {
//            if (type instanceof ParameterizedType) {
//                ParameterizedType pType = (ParameterizedType) type;
//                if (pType.getRawType().equals(BaseMapper.class)) {
//                    Type actualTypeArg = pType.getActualTypeArguments()[0];
//                    if (actualTypeArg instanceof Class) {
//                        return (Class<?>) actualTypeArg;
//                    }
//                }
//            }
//        }
//        throw new IllegalArgumentException("无法解析Mapper的实体类型: " + mapper.getClass());
//    }
//
//    /**
//     * 获取类对应的Mapper
//     */
//    public BaseMapper<?> getMapperByClass(Class<?> clazz) {
//        BaseMapper<?> mapper = entityMapperCache.get(clazz);
//        if (mapper == null) {
//            throw new RuntimeException("未找到对应Mapper，实体类: " + clazz.getName());
//        }
//        return mapper;
//    }
//
//    /**
//     * 根据类的完整名称获取类实例
//     */
//    public Class<?> getClassByFullName(String className) throws ClassNotFoundException {
//        return Class.forName(className);
//    }
//
//    /**
//     * 获取类对应的Mapper
//     */
////    public BaseMapper<?> getMapperByClass(Class<?> clazz) {
////        return mapperMap.values().stream()
////                .filter(mapper -> {
////                    for (Type type : mapper.getClass().getGenericInterfaces()) {
////                        System.out.println("");
////                        if (type instanceof ParameterizedType) {
////                            ParameterizedType parameterizedType = (ParameterizedType) type;
////                            // 检查 ParameterizedType 的实际类型参数是否与 clazz 匹配
////                            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
////                            if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class) {
////                                Class<?> actualType = (Class<?>) actualTypeArguments[0];
////                                return actualType.equals(clazz);
////                            }
////                        }
////                    }
////                    return false;
////                })
////                .findFirst()
////                .orElseThrow(() -> new RuntimeException("未找到对应Mapper"));
////    }
//
//    /**
//     * 插入数据
//     */
//    public <T> boolean insert(String className, T entity) {
//        try {
//            Class<?> clazz = getClassByFullName(className);
//            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
//            if (entity instanceof Map) {
//                Object entityObj = clazz.getDeclaredConstructor().newInstance();
//                // 使用BeanUtils.populate自动填充实体类字段
//                org.apache.commons.beanutils.BeanUtils.populate(entityObj, (Map<String, ?>) entity);
//                entity = (T) entityObj;
//            }
//            logger.info("Inserting entity: {}", entity);
//            return mapper.insert(entity) > 0;
//        } catch (Exception e) {
//            logger.error("插入失败", e);
//            throw new RuntimeException("插入失败", e);
//        }
//    }
//
//    /**
//     * 插入数据并返回主键ID
//     */
//    public <T> Serializable insertReturnId(String className, T entity) {
//        try {
//            Class<?> clazz = getClassByFullName(className);
//            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
//
//            // 处理Map类型参数
//            if (entity instanceof Map) {
//                Object entityObj = clazz.getDeclaredConstructor().newInstance();
//                BeanUtils.populate(entityObj, (Map<String, ?>) entity);
//                entity = (T) entityObj;
//            }
//
//            // 执行插入操作
//            int result = mapper.insert(entity);
//            if (result <= 0) {
//                throw new RuntimeException("插入失败，影响行数为0");
//            }
//
//            // 获取插入后的主键值
//            return extractEntityId(entity);
//        } catch (Exception e) {
//            logger.error("插入失败", e);
//            throw new RuntimeException("插入失败", e);
//        }
//    }
//
//    public <T> Serializable insertReturnIdHigh(String className, T entity) {
//        try {
//            Class<?> clazz = getClassByFullName(className);
//            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
//
//            // 处理Map类型参数转换
//            if (entity instanceof Map) {
//                logger.info("处理Map类型参数转换，转为实体类: {}", clazz.getName());
//                Object entityObj;
//                try {
//                    entityObj = clazz.getDeclaredConstructor().newInstance();
//
//                    // 使用我们自己的方法填充实体，而不是依赖BeanUtils
//                    mapToBean((Map<String, Object>) entity, entityObj);
//
//                    entity = (T) entityObj;
//                } catch (Exception e) {
//                    logger.error("转换实体对象失败: " + e.getMessage(), e);
//                    throw new RuntimeException("转换实体对象失败", e);
//                }
//            }
//
//            // 执行插入操作
//            int result = mapper.insert(entity);
//            if (result <= 0) {
//                throw new RuntimeException("插入失败，影响行数为0");
//            }
//
//            // 使用缓存机制获取主键字段
//            Field idField = getIdField(entity.getClass());
//            idField.setAccessible(true);
//            Object idValue = idField.get(entity);
//
//            if (!(idValue instanceof Serializable)) {
//                throw new RuntimeException("主键类型未实现Serializable接口");
//            }
//
//            logger.info("插入成功，生成ID：{}", idValue);
//            return (Serializable) idValue;
//        } catch (RuntimeException e) {
//            throw e;
//        } catch (Exception e) {
//            logger.error("插入操作异常", e);
//            throw new RuntimeException("系统内部异常", e);
//        }
//    }
//
//    /**
//     * 删除数据
//     */
//    public <T> boolean delete(String className, Object id) {
//        try {
//            Class<?> clazz = getClassByFullName(className);
//            BaseMapper<T> mapper = (BaseMapper<T>) getMapperByClass(clazz);
//            return mapper.deleteById((Serializable) id) > 0;
//        } catch (Exception e) {
//            throw new RuntimeException("删除失败", e);
//        }
//    }
//
//    /**
//     * 更新数据
//     */
//    public <T> boolean update(String className, T entity) {
//        try {
//            Class<?> clazz = getClassByFullName(className);
//            BaseMapper<T> mapper = (BaseMapper<T>) getMapperByClass(clazz);
//            return mapper.updateById(entity) > 0;
//        } catch (Exception e) {
//            throw new RuntimeException("更新失败", e);
//        }
//    }
//
//    /**
//     * 查询数据
//     */
//    public <T> T selectById(String className, Object id) {
//        try {
//            Class<?> clazz = getClassByFullName(className);
//            BaseMapper<T> mapper = (BaseMapper<T>) getMapperByClass(clazz);
//            return mapper.selectById((Serializable) id);
//        } catch (Exception e) {
//            throw new RuntimeException("查询失败", e);
//        }
//    }
//
//    private Field getIdField(Class<?> clazz) {
//        return ID_FIELD_CACHE.computeIfAbsent(clazz, c -> {
//            // 先查找@TableId注解字段
//            for (Field field : c.getDeclaredFields()) {
//                if (field.isAnnotationPresent(TableId.class)) {
//                    field.setAccessible(true);
//                    return field;
//                }
//            }
//            // 再尝试默认id字段
//            try {
//                Field f = c.getDeclaredField("id");
//                f.setAccessible(true);
//                return f;
//            } catch (NoSuchFieldException e) {
//                throw new RuntimeException("未找到主键字段", e);
//            }
//        });
//    }
//
//    /**
//     * 通过反射提取实体类的主键值
//     */
//    private Serializable extractEntityIdByReflex(Object entity) {
//        try {
//            // 方案1：优先尝试获取@TableId注解标记的字段
//            Class<?> clazz = entity.getClass();
//            for (Field field : clazz.getDeclaredFields()) {
//                TableId tableId = field.getAnnotation(TableId.class);
//                if (tableId != null) {
//                    field.setAccessible(true);
//                    return (Serializable) field.get(entity);
//                }
//            }
//
//            // 方案2：尝试默认的"id"字段
//            Field idField = clazz.getDeclaredField("id");
//            idField.setAccessible(true);
//            return (Serializable) idField.get(entity);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            throw new RuntimeException("无法提取实体主键，请确保实体类包含@TableId注解或id字段", e);
//        }
//    }
//
//    /**
//     * 获取Long类型主键（类型安全）
//     */
//    public <T> Long insertWithLongId(String className, T entity) {
//        Serializable id = insertReturnIdHigh(className, entity);
//        if (id instanceof Long) {
//            return (Long) id;
//        }
//        throw new TypeMismatchException("ID_TYPE_MISMATCH",
//                "期望Long类型主键，实际类型: " + id.getClass().getSimpleName());
//    }
//
//    /**
//     * 获取字符串类型主键（通用转换）
//     */
//    public <T> String insertWithStringId(String className, T entity) {
//        Serializable id = insertReturnIdHigh(className, entity);
//        return convertIdToString(id);
//    }
//
//    /******************** 核心私有方法 ********************/
//    private void initializeMapperCache() {
//        applicationContext.getBeansOfType(BaseMapper.class).values().forEach(mapper -> {
//            Class<?> entityClass = getEntityClass(mapper);
//            entityMapperCache.put(entityClass, mapper);
//            cacheIdField(entityClass); // 预缓存ID字段
//        });
//    }
//
//    private Class<?> resolveMapperEntityClass(BaseMapper<?> mapper) {
//        Type[] interfaces = mapper.getClass().getGenericInterfaces();
//        for (Type type : interfaces) {
//            if (type instanceof ParameterizedType) {
//                ParameterizedType pType = (ParameterizedType) type;
//                if (BaseMapper.class.equals(pType.getRawType())) {
//                    Type actualType = pType.getActualTypeArguments()[0];
//                    if (actualType instanceof Class) {
//                        return (Class<?>) actualType;
//                    }
//                }
//            }
//        }
//        throw new IllegalStateException("无法解析Mapper的实体类型: " + mapper.getClass().getName());
//    }
//
//    private void cacheIdField(Class<?> entityClass) {
//        idFieldCache.computeIfAbsent(entityClass, clazz -> {
//            List<Field> idFields = new ArrayList<>();
//
//            // 扫描类及其父类
//            Class<?> targetClass = clazz;
//            while (targetClass != null && targetClass != Object.class) {
//                Arrays.stream(targetClass.getDeclaredFields())
//                        .filter(f -> f.isAnnotationPresent(TableId.class))
//                        .findFirst()
//                        .ifPresent(idFields::add);
//                targetClass = targetClass.getSuperclass();
//            }
//
//            // 找到注解字段
//            if (!idFields.isEmpty()) {
//                Field field = idFields.get(0);
//                field.setAccessible(true);
//                return field;
//            }
//
//            // 查找默认ID字段
//            try {
//                Field field = clazz.getDeclaredField("id");
//                field.setAccessible(true);
//                return field;
//            } catch (NoSuchFieldException e) {
//                logger.warn("实体类未定义主键字段: {}", clazz.getName());
//                return null;
//            }
//        });
//    }
//
//    private Object processInputEntity(Object entity, Class<?> targetClass) throws Exception {
//        if (entity instanceof Map) {
//            Object instance = targetClass.getDeclaredConstructor().newInstance();
//            BeanUtils.populate(instance, (Map<String, ? extends Object>) entity);
//            return instance;
//        }
//        return entity;
//    }
//
//    private Serializable extractEntityId(Object entity) {
//        try {
//            Field idField = idFieldCache.get(entity.getClass());
//            if (idField == null) {
//                throw new IllegalStateException("未缓存的主键字段: " + entity.getClass().getName());
//            }
//            Object idValue = idField.get(entity);
//            if (!(idValue instanceof Serializable)) {
//                throw new TypeMismatchException("ID_NOT_SERIALIZABLE",
//                        "主键类型未实现Serializable: " + idValue.getClass().getName());
//            }
//            return (Serializable) idValue;
//        } catch (IllegalAccessException e) {
//            throw new DynamicServiceException("ID_EXTRACTION_FAILED", "主键提取失败", e);
//        }
//    }
//
//    /******************** 类型转换模块 ********************/
//
//    private void registerDefaultConverters() {
//        registerConverter(Long.class, Object::toString);
//        registerConverter(String.class, s -> (String) s);
//        registerConverter(Integer.class, Object::toString);
//        registerConverter(UUID.class, u -> ((UUID)u).toString().replace("-", ""));
//    }
//
//    /**
//     * 替换原来的registerDateConverter方法，更全面地解决日期转换问题
//     */
//    private void setupBeanUtilsDateConverter() {
//        try {
//            // 创建一个新的DateConverter实例，传入null作为默认值
//            DateConverter converter = new DateConverter(null);
//
//            // 设置支持的日期格式
//            String[] patterns = {
//                "yyyy-MM-dd",
//                "yyyy-MM-dd HH:mm:ss",
//                "yyyy/MM/dd",
//                "yyyy/MM/dd HH:mm:ss",
//                "MM/dd/yyyy",
//                "dd/MM/yyyy",
//                "yyyy.MM.dd",
//                "yyyy.MM.dd HH:mm:ss",
//                "yyyyMMdd",
//                "yyyyMMddHHmmss"
//            };
//
//            // 设置日期格式模式
//            converter.setPatterns(patterns);
//
//            // 注册到ConvertUtils中
//            ConvertUtils.register(converter, Date.class);
//            ConvertUtils.register(converter, java.sql.Date.class);
//            ConvertUtils.register(converter, java.sql.Timestamp.class);
//
//            logger.info("已成功配置日期转换器，支持的格式: {}", Arrays.toString(patterns));
//        } catch (Exception e) {
//            logger.error("配置日期转换器失败", e);
//            throw new RuntimeException("配置日期转换器失败", e);
//        }
//    }
//
//    public void registerConverter(Class<?> idType, Function<Serializable, String> converter) {
//        idConverters.put(idType, converter);
//        logger.info("注册主键转换器: {} -> {}", idType.getSimpleName(), converter.getClass().getSimpleName());
//    }
//
//    private String convertIdToString(Serializable id) {
//        Function<Serializable, String> converter = idConverters.get(id.getClass());
//        if (converter != null) {
//            return converter.apply(id);
//        }
//
//        // 默认转换策略
//        String converted = id.toString();
//        logger.debug("使用默认转换器处理类型: {} => {}",
//                id.getClass().getSimpleName(), converted);
//        return converted;
//    }
//
//    /******************** 辅助方法 ********************/
//
//    private Class<?> resolveEntityClass(String className) {
//        try {
//            return Class.forName(className);
//        } catch (ClassNotFoundException e) {
//            throw new DynamicServiceException("CLASS_NOT_FOUND", "类未找到: " + className, e);
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    private BaseMapper<Object> getCachedMapper(Class<?> entityClass) {
//        BaseMapper<?> mapper = entityMapperCache.get(entityClass);
//        if (mapper == null) {
//            throw new DynamicServiceException("MAPPER_NOT_FOUND",
//                    "未注册的实体类Mapper: " + entityClass.getName());
//        }
//        return (BaseMapper<Object>) mapper;
//    }
//
//    private void executeInsert(BaseMapper<Object> mapper, Object entity) {
//        int affected = mapper.insert(entity);
//        if (affected <= 0) {
//            throw new DynamicServiceException("INSERT_FAILED",
//                    "数据库插入失败，影响行数: " + affected);
//        }
//        logger.debug("插入成功，影响行数: {}", affected);
//    }
//
//    /**
//     * 预处理Map中的日期字段
//     */
//    private Map<String, Object> preprocessDateFields(Map<String, Object> sourceMap, Class<?> targetClass) {
//        Map<String, Object> result = new HashMap<>(sourceMap);
//
//        // 检查目标类中的每个字段
//        for (Field field : getAllFields(targetClass)) {
//            String fieldName = field.getName();
//
//            // 只处理日期类型的字段
//            if (Date.class.isAssignableFrom(field.getType()) && result.containsKey(fieldName)) {
//                Object value = result.get(fieldName);
//
//                // 如果字段值是字符串，但目标是日期类型，则移除该字段，让BeanUtils的转换器处理
//                if (value instanceof String && ((String) value).trim().isEmpty()) {
//                    // 对于空字符串，直接设置为null
//                    result.put(fieldName, null);
//                    logger.debug("字段 '{}' 是空字符串，已设置为null", fieldName);
//                }
//            }
//        }
//
//        return result;
//    }
//
//    /**
//     * 获取类及其所有父类的所有字段
//     */
//    private List<Field> getAllFields(Class<?> clazz) {
//        List<Field> fields = new ArrayList<>();
//        Class<?> currentClass = clazz;
//
//        while (currentClass != null && currentClass != Object.class) {
//            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
//            currentClass = currentClass.getSuperclass();
//        }
//
//        return fields;
//    }
//
//    /**
//     * 手动将Map转换为Bean对象，解决日期转换问题
//     */
//    private void mapToBean(Map<String, Object> map, Object bean) throws Exception {
//        Class<?> beanClass = bean.getClass();
//
//        // 获取所有字段（包括父类）
//        for (Field field : getAllFields(beanClass)) {
//            field.setAccessible(true);
//            String fieldName = field.getName();
//
//            // 如果Map中包含该字段
//            if (map.containsKey(fieldName)) {
//                Object value = map.get(fieldName);
//
//                // 特殊处理日期类型
//                if (value != null && (field.getType() == Date.class ||
//                        field.getType() == java.sql.Date.class ||
//                        field.getType() == java.sql.Timestamp.class)) {
//                    if (value instanceof String) {
//                        String strValue = (String) value;
//                        // 空字符串转为null
//                        if (strValue.trim().isEmpty()) {
//                            field.set(bean, null);
//                        } else {
//                            // 尝试解析日期
//                            try {
//                                Date date = parseDate(strValue);
//
//                                // 根据字段类型转换
//                                if (field.getType() == java.sql.Date.class) {
//                                    field.set(bean, new java.sql.Date(date.getTime()));
//                                } else if (field.getType() == java.sql.Timestamp.class) {
//                                    field.set(bean, new java.sql.Timestamp(date.getTime()));
//                                } else {
//                                    field.set(bean, date);
//                                }
//                            } catch (Exception e) {
//                                logger.warn("无法解析日期字段 '{}' 的值 '{}'，设置为null", fieldName, strValue);
//                                field.set(bean, null);
//                            }
//                        }
//                    } else if (value instanceof Date) {
//                        // 已经是Date类型，可能需要转换为特定日期类型
//                        Date dateValue = (Date) value;
//                        if (field.getType() == java.sql.Date.class) {
//                            field.set(bean, new java.sql.Date(dateValue.getTime()));
//                        } else if (field.getType() == java.sql.Timestamp.class) {
//                            field.set(bean, new java.sql.Timestamp(dateValue.getTime()));
//                        } else {
//                            field.set(bean, dateValue);
//                        }
//                    } else {
//                        logger.warn("字段 '{}' 值类型不兼容，期望Date或String，实际是: {}", fieldName, value.getClass().getName());
//                        field.set(bean, null);
//                    }
//                }
//                // 处理其他类型（非日期类型）
//                else if (value != null) {
//                    try {
//                        // 使用BeanUtils转换普通类型
//                        Object convertedValue = ConvertUtils.convert(value, field.getType());
//                        field.set(bean, convertedValue);
//                    } catch (Exception e) {
//                        logger.warn("字段 '{}' 值 '{}' 转换失败: {}", fieldName, value, e.getMessage());
//                        // 如果转换失败，对象型字段设置为null，基本类型保持默认值
//                        if (!field.getType().isPrimitive()) {
//                            field.set(bean, null);
//                        }
//                    }
//                } else {
//                    // 值为null，对象型字段设置为null，基本类型保持默认值
//                    if (!field.getType().isPrimitive()) {
//                        field.set(bean, null);
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * 尝试用多种格式解析日期字符串
//     */
//    private Date parseDate(String dateStr) throws Exception {
//        String[] patterns = {
//                "yyyy-MM-dd",
//                "yyyy-MM-dd HH:mm:ss",
//                "yyyy/MM/dd",
//                "yyyy/MM/dd HH:mm:ss",
//                "MM/dd/yyyy",
//                "dd/MM/yyyy",
//                "yyyy.MM.dd",
//                "yyyy.MM.dd HH:mm:ss",
//                "yyyyMMdd",
//                "yyyyMMddHHmmss"
//        };
//
//        // 移除两端可能的空白字符
//        dateStr = dateStr.trim();
//
//        // 尝试不同的日期格式
//        for (String pattern : patterns) {
//            try {
//                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pattern);
//                sdf.setLenient(false); // 严格模式
//                return sdf.parse(dateStr);
//            } catch (Exception e) {
//                // 继续尝试下一个格式
//            }
//        }
//
//        // 如果所有格式都失败，抛出异常
//        throw new IllegalArgumentException("无法解析日期: " + dateStr);
//    }

}