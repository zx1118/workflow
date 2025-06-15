package com.epiroc.workflow.common.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epiroc.workflow.common.system.exception.DynamicServiceException;
import com.epiroc.workflow.common.system.exception.TypeMismatchException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConversionException;
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
public class TestService {

    private static final Logger logger = LoggerFactory.getLogger(TestService.class);

    @Autowired
    private ApplicationContext applicationContext;

    private Map<Class<?>, BaseMapper<?>> entityMapperMap = new HashMap<>();

    // 缓存实体类的主键字段信息（提升反射性能）
    private static final Map<Class<?>, Field> ID_FIELD_CACHE = new ConcurrentHashMap<>();

    private final Map<Class<?>, Function<Serializable, String>> idConverters = new ConcurrentHashMap<>();

    private final Map<Class<?>, BaseMapper<?>> entityMapperCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Field> idFieldCache = new ConcurrentHashMap<>();

    // 初始化阶段加载所有Mapper
    @PostConstruct
    public void init() {
        initializeMapperCache();
        registerDefaultConverters();
        registerDateConverter(); // 注册Date转换器
        logger.info("DynamicService initialized with {} mappers", entityMapperCache.size());
    }

    private Class<?> getEntityClass(BaseMapper<?> mapper) {
        // 获取Mapper接口的Class（假设第一个接口是具体的Mapper接口）
        Class<?> mapperInterface = mapper.getClass().getInterfaces()[0];
        // 获取Mapper接口的泛型父接口
        Type[] genericInterfaces = mapperInterface.getGenericInterfaces();
        for (Type type : genericInterfaces) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                if (pType.getRawType().equals(BaseMapper.class)) {
                    Type actualTypeArg = pType.getActualTypeArguments()[0];
                    if (actualTypeArg instanceof Class) {
                        return (Class<?>) actualTypeArg;
                    }
                }
            }
        }
        throw new IllegalArgumentException("无法解析Mapper的实体类型: " + mapper.getClass());
    }

    /**
     * 获取类对应的Mapper
     */
    public BaseMapper<?> getMapperByClass(Class<?> clazz) {
        BaseMapper<?> mapper = entityMapperCache.get(clazz);
        if (mapper == null) {
            throw new RuntimeException("未找到对应Mapper，实体类: " + clazz.getName());
        }
        return mapper;
    }

    /**
     * 根据类的完整名称获取类实例
     */
    public Class<?> getClassByFullName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    /**
     * 插入数据
     */
    public <T> boolean insert(String className, T entity) {
        try {
            Class<?> clazz = getClassByFullName(className);
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            if (entity instanceof Map) {
                Object entityObj = clazz.getDeclaredConstructor().newInstance();
                // 使用BeanUtils.populate自动填充实体类字段
                org.apache.commons.beanutils.BeanUtils.populate(entityObj, (Map<String, ?>) entity);
                entity = (T) entityObj;
            }
            logger.info("Inserting entity: {}", entity);
            return mapper.insert(entity) > 0;
        } catch (Exception e) {
            logger.error("插入失败", e);
            throw new RuntimeException("插入失败", e);
        }
    }

    /**
     * 插入数据并返回主键ID
     */
    public <T> Serializable insertReturnId(String className, T entity) {
        try {
            Class<?> clazz = getClassByFullName(className);
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);

            // 处理Map类型参数
            if (entity instanceof Map) {
                Object entityObj = clazz.getDeclaredConstructor().newInstance();
                BeanUtils.populate(entityObj, (Map<String, ?>) entity);
                entity = (T) entityObj;
            }

            // 执行插入操作
            int result = mapper.insert(entity);
            if (result <= 0) {
                throw new RuntimeException("插入失败，影响行数为0");
            }

            // 获取插入后的主键值
            return extractEntityId(entity);
        } catch (Exception e) {
            logger.error("插入失败", e);
            throw new RuntimeException("插入失败", e);
        }
    }

    public <T> Serializable insertReturnIdHigh(String className, T entity) {
        try {
            Class<?> clazz = resolveEntityClass(className);
            BaseMapper<Object> mapper = getCachedMapper(clazz);

            // 强化类型转换过程
            Object processedEntity = processEntityWithDateCheck(clazz, entity);

            executeInsert(mapper, processedEntity);
            return extractEntityId(processedEntity);
        } catch (ConversionException e) {
            throw new DynamicServiceException("DATA_CONVERSION_FAILED",
                    "字段[" + e.getMessage() + "]转换失败，值：" + e.toString(), e);
        } catch (Exception e) {
            throw new RuntimeException("持久化操作失败", e);
        }
    }

    // 新增辅助方法：带日期类型检查的实体处理
    private Object processEntityWithDateCheck(Class<?> clazz, Object entity) throws Exception {
        if (!(entity instanceof Map)) return entity;

        Map<String, ?> sourceMap = (Map<String, ?>) entity;
        Object instance = clazz.getDeclaredConstructor().newInstance();

        for (Map.Entry<String, ?> entry : sourceMap.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            try {
                Field field = clazz.getDeclaredField(fieldName);
                if (field.getType() == Date.class && value instanceof String) {
                    logger.debug("正在进行日期字段转换：{}={}", fieldName, value);
                }
            } catch (NoSuchFieldException ex) {
                logger.warn("实体类 {} 不存在字段：{}", clazz.getSimpleName(), fieldName);
            }
        }

        BeanUtils.populate(instance, sourceMap);
        return instance;
    }

    /**
     * 删除数据
     */
    public <T> boolean delete(String className, Object id) {
        try {
            Class<?> clazz = getClassByFullName(className);
            BaseMapper<T> mapper = (BaseMapper<T>) getMapperByClass(clazz);
            return mapper.deleteById((Serializable) id) > 0;
        } catch (Exception e) {
            throw new RuntimeException("删除失败", e);
        }
    }

    /**
     * 更新数据
     */
    public <T> boolean update(String className, T entity) {
        try {
            Class<?> clazz = getClassByFullName(className);
            BaseMapper<T> mapper = (BaseMapper<T>) getMapperByClass(clazz);
            return mapper.updateById(entity) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新失败", e);
        }
    }

    /**
     * 查询数据
     */
    public <T> T selectById(String className, Object id) {
        try {
            Class<?> clazz = getClassByFullName(className);
            BaseMapper<T> mapper = (BaseMapper<T>) getMapperByClass(clazz);
            return mapper.selectById((Serializable) id);
        } catch (Exception e) {
            throw new RuntimeException("查询失败", e);
        }
    }

    private Field getIdField(Class<?> clazz) {
        return ID_FIELD_CACHE.computeIfAbsent(clazz, c -> {
            // 先查找@TableId注解字段
            for (Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(TableId.class)) {
                    field.setAccessible(true);
                    return field;
                }
            }
            // 再尝试默认id字段
            try {
                Field f = c.getDeclaredField("id");
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("未找到主键字段", e);
            }
        });
    }

    /**
     * 通过反射提取实体类的主键值
     */
    private Serializable extractEntityIdByReflex(Object entity) {
        try {
            // 方案1：优先尝试获取@TableId注解标记的字段
            Class<?> clazz = entity.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                TableId tableId = field.getAnnotation(TableId.class);
                if (tableId != null) {
                    field.setAccessible(true);
                    return (Serializable) field.get(entity);
                }
            }

            // 方案2：尝试默认的"id"字段
            Field idField = clazz.getDeclaredField("id");
            idField.setAccessible(true);
            return (Serializable) idField.get(entity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("无法提取实体主键，请确保实体类包含@TableId注解或id字段", e);
        }
    }

    /**
     * 获取Long类型主键（类型安全）
     */
    public <T> Long insertWithLongId(String className, T entity) {
        Serializable id = insertReturnIdHigh(className, entity);
        if (id instanceof Long) {
            return (Long) id;
        }
        throw new TypeMismatchException("ID_TYPE_MISMATCH",
                "期望Long类型主键，实际类型: " + id.getClass().getSimpleName());
    }

    /**
     * 获取字符串类型主键（通用转换）
     */
    public <T> String insertWithStringId(String className, T entity) {
        Serializable id = insertReturnIdHigh(className, entity);
        return convertIdToString(id);
    }

    /******************** 核心私有方法 ********************/
    private void initializeMapperCache() {
        applicationContext.getBeansOfType(BaseMapper.class).values().forEach(mapper -> {
            Class<?> entityClass = getEntityClass(mapper);
            entityMapperCache.put(entityClass, mapper);
            cacheIdField(entityClass); // 预缓存ID字段
        });
    }

    private Class<?> resolveMapperEntityClass(BaseMapper<?> mapper) {
        Type[] interfaces = mapper.getClass().getGenericInterfaces();
        for (Type type : interfaces) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                if (BaseMapper.class.equals(pType.getRawType())) {
                    Type actualType = pType.getActualTypeArguments()[0];
                    if (actualType instanceof Class) {
                        return (Class<?>) actualType;
                    }
                }
            }
        }
        throw new IllegalStateException("无法解析Mapper的实体类型: " + mapper.getClass().getName());
    }

    private void cacheIdField(Class<?> entityClass) {
        idFieldCache.computeIfAbsent(entityClass, clazz -> {
            List<Field> idFields = new ArrayList<>();

            // 扫描类及其父类
            Class<?> targetClass = clazz;
            while (targetClass != null && targetClass != Object.class) {
                Arrays.stream(targetClass.getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(TableId.class))
                        .findFirst()
                        .ifPresent(idFields::add);
                targetClass = targetClass.getSuperclass();
            }

            // 找到注解字段
            if (!idFields.isEmpty()) {
                Field field = idFields.get(0);
                field.setAccessible(true);
                return field;
            }

            // 查找默认ID字段
            try {
                Field field = clazz.getDeclaredField("id");
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                logger.warn("实体类未定义主键字段: {}", clazz.getName());
                return null;
            }
        });
    }

    private Object processInputEntity(Object entity, Class<?> targetClass) throws Exception {
        if (entity instanceof Map) {
            Object instance = targetClass.getDeclaredConstructor().newInstance();
            Map<String, ?> sourceMap = (Map<String, ?>) entity;

            // 类型安全检查
            for (Map.Entry<String, ?> entry : sourceMap.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                try {
                    Field field = targetClass.getDeclaredField(fieldName);
                    if (value instanceof String && field.getType() == Date.class) {
                        logger.debug("正在进行日期字段转换: {}={}", fieldName, value);
                    }
                } catch (NoSuchFieldException e) {
                    logger.warn("实体类{}不存在字段: {}", targetClass.getSimpleName(), fieldName);
                }
            }

            BeanUtils.populate(instance, sourceMap);
            return instance;
        }
        return entity;
    }

    private Serializable extractEntityId(Object entity) {
        try {
            Field idField = idFieldCache.get(entity.getClass());
            if (idField == null) {
                throw new IllegalStateException("未缓存的主键字段: " + entity.getClass().getName());
            }
            Object idValue = idField.get(entity);
            if (!(idValue instanceof Serializable)) {
                throw new TypeMismatchException("ID_NOT_SERIALIZABLE",
                        "主键类型未实现Serializable: " + idValue.getClass().getName());
            }
            return (Serializable) idValue;
        } catch (IllegalAccessException e) {
            throw new DynamicServiceException("ID_EXTRACTION_FAILED", "主键提取失败", e);
        }
    }

    /******************** 类型转换模块 ********************/

    private void registerDefaultConverters() {
        registerConverter(Long.class, Object::toString);
        registerConverter(String.class, s -> (String) s);
        registerConverter(Integer.class, Object::toString);
        registerConverter(UUID.class, u -> ((UUID)u).toString().replace("-", ""));
    }


    // DynamicService.java 新增代码
    private void registerDateConverter() {
        // 支持多种日期格式
        String[] patterns = {
                "yyyy-MM-dd",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy/MM/dd",
                "yyyyMMdd",
                "MM/dd/yyyy"
        };

        DateConverter dateConverter = new DateConverter(null);
        dateConverter.setPatterns(patterns);

        // 允许空值和非法格式报错
//        dateConverter.setLenient(false);

        // 注册到BeanUtils
        ConvertUtils.register(dateConverter, Date.class);

        logger.info("注册日期转换器，支持格式: {}", Arrays.toString(patterns));
    }

    public void registerConverter(Class<?> idType, Function<Serializable, String> converter) {
        idConverters.put(idType, converter);
        logger.info("注册主键转换器: {} -> {}", idType.getSimpleName(), converter.getClass().getSimpleName());
    }

    private String convertIdToString(Serializable id) {
        Function<Serializable, String> converter = idConverters.get(id.getClass());
        if (converter != null) {
            return converter.apply(id);
        }

        // 默认转换策略
        String converted = id.toString();
        logger.debug("使用默认转换器处理类型: {} => {}",
                id.getClass().getSimpleName(), converted);
        return converted;
    }

    /******************** 辅助方法 ********************/

    private Class<?> resolveEntityClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new DynamicServiceException("CLASS_NOT_FOUND", "类未找到: " + className, e);
        }
    }

    @SuppressWarnings("unchecked")
    private BaseMapper<Object> getCachedMapper(Class<?> entityClass) {
        BaseMapper<?> mapper = entityMapperCache.get(entityClass);
        if (mapper == null) {
            throw new DynamicServiceException("MAPPER_NOT_FOUND",
                    "未注册的实体类Mapper: " + entityClass.getName());
        }
        return (BaseMapper<Object>) mapper;
    }

    private void executeInsert(BaseMapper<Object> mapper, Object entity) {
        int affected = mapper.insert(entity);
        if (affected <= 0) {
            throw new DynamicServiceException("INSERT_FAILED",
                    "数据库插入失败，影响行数: " + affected);
        }
        logger.debug("插入成功，影响行数: {}", affected);
    }



}