package com.epiroc.workflow.common.service;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.epiroc.workflow.common.system.exception.DynamicServiceException;
import com.epiroc.workflow.common.system.exception.TypeMismatchException;
import com.epiroc.workflow.common.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
@Slf4j
public class WorkflowDynamicService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowDynamicService.class);

    @Autowired
    private ApplicationContext applicationContext;

    private Map<Class<?>, BaseMapper<?>> entityMapperMap = new HashMap<>();

    // 缓存实体类的主键字段信息（提升反射性能）
    private static final Map<Class<?>, Field> ID_FIELD_CACHE = new ConcurrentHashMap<>();

    private final Map<Class<?>, Function<Serializable, String>> idConverters = new ConcurrentHashMap<>();

    private final Map<Class<?>, BaseMapper<?>> entityMapperCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Field> idFieldCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, IService<?>> entityServiceCache = new ConcurrentHashMap<>();

//    @PostConstruct
//    public void init() {
//        Map<String, BaseMapper> beans = applicationContext.getBeansOfType(BaseMapper.class);
//        for (BaseMapper<?> mapper : beans.values()) {
//            Class<?> entityClass = getEntityClass(mapper);
//            entityMapperMap.put(entityClass, mapper);
//            logger.info("Mapped entity class: {} to mapper: {}", entityClass.getName(), mapper.getClass().getName());
//        }
//    }

    // 初始化阶段加载所有Mapper和Service
    @PostConstruct
    public void init() {
        initializeMapperCache();
        initializeServiceCache();
        registerDefaultConverters();
        setupBeanUtilsDateConverter();
        logger.info("DynamicService initialized with {} mappers and {} services", 
                    entityMapperCache.size(), entityServiceCache.size());
    }

    /*// 通过Service批量保存（会调用Service的业务逻辑）
    dynamicService.saveBatchByService("com.example.Entity", entityList);

    // 获取Service实例进行自定义操作
    IService<?> service = dynamicService.getServiceByClassName("com.example.Entity");*/

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
     * 获取类对应的Service
     */
    public IService<?> getServiceByClass(Class<?> clazz) {
        IService<?> service = entityServiceCache.get(clazz);
        if (service == null) {
            throw new RuntimeException("未找到对应Service，实体类: " + clazz.getName());
        }
        return service;
    }

    /**
     * 获取类对应的Service（字符串类名）
     */
    public IService<?> getServiceByClassName(String className) {
        try {
            Class<?> clazz = getClassByFullName(className);
            return getServiceByClass(clazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("类未找到: " + className, e);
        }
    }

    /**
     * 根据类的完整名称获取类实例
     */
    public static Class<?> getClassByFullName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    /**
     * 获取类对应的Mapper
     */
//    public BaseMapper<?> getMapperByClass(Class<?> clazz) {
//        return mapperMap.values().stream()
//                .filter(mapper -> {
//                    for (Type type : mapper.getClass().getGenericInterfaces()) {
//                        System.out.println("");
//                        if (type instanceof ParameterizedType) {
//                            ParameterizedType parameterizedType = (ParameterizedType) type;
//                            // 检查 ParameterizedType 的实际类型参数是否与 clazz 匹配
//                            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
//                            if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class) {
//                                Class<?> actualType = (Class<?>) actualTypeArguments[0];
//                                return actualType.equals(clazz);
//                            }
//                        }
//                    }
//                    return false;
//                })
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("未找到对应Mapper"));
//    }

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
                BeanUtils.populate(entityObj, (Map<String, ?>) entity);
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
            Class<?> clazz = getClassByFullName(className);
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);

            // 处理Map类型参数转换
            if (entity instanceof Map) {
                logger.info("处理Map类型参数转换，转为实体类: {}", clazz.getName());
                Object entityObj;
                try {
                    entityObj = clazz.getDeclaredConstructor().newInstance();

                    // 使用我们自己的方法填充实体，而不是依赖BeanUtils
                    mapToBean((Map<String, Object>) entity, entityObj);

                    entity = (T) entityObj;
                } catch (Exception e) {
                    logger.error("转换实体对象失败: " + e.getMessage(), e);
                    throw new RuntimeException("转换实体对象失败", e);
                }
            }

            // 执行插入操作
            int result = mapper.insert(entity);
            if (result <= 0) {
                throw new RuntimeException("插入失败，影响行数为0");
            }

            // 使用缓存机制获取主键字段
            Field idField = getIdField(entity.getClass());
            idField.setAccessible(true);
            Object idValue = idField.get(entity);

            if (!(idValue instanceof Serializable)) {
                throw new RuntimeException("主键类型未实现Serializable接口");
            }

            logger.info("插入成功，生成ID：{}", idValue);
            return (Serializable) idValue;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("插入操作异常", e);
            throw new RuntimeException("系统内部异常", e);
        }
    }

    /**
     * 直接插入实体对象并返回主键ID（无需Map转换）
     * @param entity 实体对象（非Map类型）
     * @return 插入后的主键ID
     */
    public <T> Serializable insertEntityReturnId(T entity) {
        try {
            if (entity == null) {
                throw new IllegalArgumentException("实体对象不能为null");
            }

            if (entity instanceof Map) {
                throw new IllegalArgumentException("此方法不支持Map类型参数，请使用insertReturnIdHigh方法");
            }

            Class<?> clazz = entity.getClass();
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);

            logger.info("开始插入实体对象，类型: {}", clazz.getName());

            // 执行插入操作
            int result = mapper.insert(entity);
            if (result <= 0) {
                throw new RuntimeException("插入失败，影响行数为0");
            }

            // 使用缓存机制获取主键字段
            Field idField = getIdField(clazz);
            idField.setAccessible(true);
            Object idValue = idField.get(entity);

            if (!(idValue instanceof Serializable)) {
                throw new RuntimeException("主键类型未实现Serializable接口");
            }

            logger.info("插入成功，生成ID：{}", idValue);
            return (Serializable) idValue;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("插入操作异常", e);
            throw new RuntimeException("系统内部异常", e);
        }
    }

    /**
     * 直接插入实体对象并返回主键ID（通过Class类型验证）
     * @param clazz 实体类Class
     * @param entity 实体对象
     * @return 插入后的主键ID
     */
    public <T> Serializable insertEntityReturnId(Class<T> clazz, T entity) {
        try {
            if (entity == null) {
                throw new IllegalArgumentException("实体对象不能为null");
            }

            if (entity instanceof Map) {
                throw new IllegalArgumentException("此方法不支持Map类型参数，请使用insertReturnIdHigh方法");
            }

            // 验证实体类型匹配
            if (!clazz.isInstance(entity)) {
                throw new IllegalArgumentException(String.format("实体对象类型不匹配，期望: %s，实际: %s", 
                    clazz.getName(), entity.getClass().getName()));
            }

            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);

            logger.info("开始插入实体对象，类型: {}", clazz.getName());

            // 执行插入操作
            int result = mapper.insert(entity);
            if (result <= 0) {
                throw new RuntimeException("插入失败，影响行数为0");
            }

            // 使用缓存机制获取主键字段
            Field idField = getIdField(clazz);
            idField.setAccessible(true);
            Object idValue = idField.get(entity);

            if (!(idValue instanceof Serializable)) {
                throw new RuntimeException("主键类型未实现Serializable接口");
            }

            logger.info("插入成功，生成ID：{}", idValue);
            return (Serializable) idValue;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("插入操作异常", e);
            throw new RuntimeException("系统内部异常", e);
        }
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
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            
            // 处理Map类型参数转换
            if (entity instanceof Map) {
                Object entityObj = clazz.getDeclaredConstructor().newInstance();
                mapToBean((Map<String, Object>) entity, entityObj);
                entity = (T) entityObj;
            }
            
            logger.info("更新实体，类型: {}", clazz.getName());
            return mapper.updateById(entity) > 0;
        } catch (Exception e) {
            logger.error("更新失败", e);
            throw new RuntimeException("更新失败", e);
        }
    }

    /**
     * 更新数据（直接传入实体对象）
     */
    public <T> boolean update(T entity) {
        try {
            if (entity == null) {
                throw new IllegalArgumentException("实体对象不能为null");
            }

            // 处理Map类型参数转换
            Object processedEntity = entity;
            Class<?> clazz;
            
            if (entity instanceof Map) {
                throw new IllegalArgumentException("此方法不支持Map类型参数，请使用update(String className, T entity)方法");
            } else {
                clazz = entity.getClass();
            }
            
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            
            logger.info("更新实体对象，类型: {}", clazz.getName());
            return mapper.updateById(processedEntity) > 0;
        } catch (Exception e) {
            logger.error("更新失败", e);
            throw new RuntimeException("更新失败", e);
        }
    }

    /**
     * 批量更新实体列表（直接传入List<Entity>）
     * @param entityList 实体列表
     * @return 是否成功
     */
    public <T> boolean updateBatch(List<T> entityList) {
        try {
            if (entityList == null || entityList.isEmpty()) {
                logger.warn("批量更新数据为空");
                return true;
            }

            // 从第一个实体获取类型信息
            T firstEntity = entityList.get(0);
            if (firstEntity instanceof Map) {
                throw new IllegalArgumentException("此方法不支持Map类型参数，请使用updateBatchMaps方法");
            }

            Class<?> clazz = firstEntity.getClass();
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            
            logger.info("开始批量更新实体对象，类型: {}, 数据量: {}", clazz.getName(), entityList.size());
            
            // 批量更新，每次最多1000条
            int batchSize = 1000;
            int totalSize = entityList.size();
            int successCount = 0;
            
            for (int i = 0; i < totalSize; i += batchSize) {
                int endIndex = Math.min(i + batchSize, totalSize);
                List<T> batch = entityList.subList(i, endIndex);
                
                for (T entity : batch) {
                    try {
                        // 验证实体类型一致性
                        if (!clazz.isInstance(entity)) {
                            logger.warn("跳过类型不匹配的实体，期望: {}, 实际: {}", 
                                clazz.getName(), entity.getClass().getName());
                            continue;
                        }
                        
                        // 处理Map类型参数转换（虽然前面已经检查过，但为了保险起见）
                        Object processedEntity = entity;
                        if (entity instanceof Map) {
                            Object entityObj = clazz.getDeclaredConstructor().newInstance();
                            mapToBean((Map<String, Object>) entity, entityObj);
                            processedEntity = entityObj;
                        }
                        
                        int result = mapper.updateById(processedEntity);
                        if (result > 0) {
                            successCount++;
                        }
                    } catch (Exception e) {
                        logger.error("批量更新单条数据失败: {}", e.getMessage(), e);
                        // 继续处理其他数据，不中断整个批次
                    }
                }
                
                logger.debug("批量更新进度: {}/{}", Math.min(endIndex, totalSize), totalSize);
            }
            
            logger.info("批量更新完成，成功: {}/{}", successCount, totalSize);
            return successCount > 0; // 只要有成功的就返回true
            
        } catch (Exception e) {
            logger.error("批量更新失败", e);
            throw new RuntimeException("批量更新失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量更新实体列表并返回更新成功的数量
     * @param entityList 实体列表
     * @return 更新成功的数量
     */
    public <T> int updateBatchCount(List<T> entityList) {
        try {
            if (entityList == null || entityList.isEmpty()) {
                logger.warn("批量更新数据为空");
                return 0;
            }

            // 从第一个实体获取类型信息
            T firstEntity = entityList.get(0);
            if (firstEntity instanceof Map) {
                throw new IllegalArgumentException("此方法不支持Map类型参数，请使用updateBatchMapsCount方法");
            }

            Class<?> clazz = firstEntity.getClass();
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            
            logger.info("开始批量更新实体对象（返回计数），类型: {}, 数据量: {}", clazz.getName(), entityList.size());
            
            int successCount = 0;
            
            for (T entity : entityList) {
                try {
                    // 验证实体类型一致性
                    if (!clazz.isInstance(entity)) {
                        logger.warn("跳过类型不匹配的实体，期望: {}, 实际: {}", 
                            clazz.getName(), entity.getClass().getName());
                        continue;
                    }
                    
                    // 处理Map类型参数转换（虽然前面已经检查过，但为了保险起见）
                    Object processedEntity = entity;
                    if (entity instanceof Map) {
                        Object entityObj = clazz.getDeclaredConstructor().newInstance();
                        mapToBean((Map<String, Object>) entity, entityObj);
                        processedEntity = entityObj;
                    }
                    
                    int result = mapper.updateById(processedEntity);
                    if (result > 0) {
                        successCount++;
                    }
                } catch (Exception e) {
                    logger.error("批量更新单条数据失败: {}", e.getMessage(), e);
                    // 继续处理其他数据
                }
            }
            
            logger.info("批量更新完成，成功更新: {} 条", successCount);
            return successCount;
            
        } catch (Exception e) {
            logger.error("批量更新失败", e);
            throw new RuntimeException("批量更新失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量更新Map列表（需要指定实体类名）
     * @param className 实体类全名
     * @param mapList Map列表
     * @return 是否成功
     */
    public boolean updateBatchMaps(String className, List<Map<String, Object>> mapList) {
        try {
            if (mapList == null || mapList.isEmpty()) {
                logger.warn("批量更新Map数据为空，类名: {}", className);
                return true;
            }

            Class<?> clazz = getClassByFullName(className);
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            
            logger.info("开始批量更新Map数据，类名: {}, 数据量: {}", className, mapList.size());
            
            // 批量更新，每次最多1000条
            int batchSize = 1000;
            int totalSize = mapList.size();
            int successCount = 0;
            
            for (int i = 0; i < totalSize; i += batchSize) {
                int endIndex = Math.min(i + batchSize, totalSize);
                List<Map<String, Object>> batch = mapList.subList(i, endIndex);
                
                for (Map<String, Object> map : batch) {
                    try {
                        // 将Map转换为实体对象
                        Object entityObj = clazz.getDeclaredConstructor().newInstance();
                        mapToBean(map, entityObj);
                        
                        int result = mapper.updateById(entityObj);
                        if (result > 0) {
                            successCount++;
                        }
                    } catch (Exception e) {
                        logger.error("批量更新单条Map数据失败: {}", e.getMessage(), e);
                        // 继续处理其他数据，不中断整个批次
                    }
                }
                
                logger.debug("批量更新Map进度: {}/{}", Math.min(endIndex, totalSize), totalSize);
            }
            
            logger.info("批量更新Map完成，成功: {}/{}", successCount, totalSize);
            return successCount > 0; // 只要有成功的就返回true
            
        } catch (Exception e) {
            logger.error("批量更新Map失败，类名: {}", className, e);
            throw new RuntimeException("批量更新Map失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量更新Map列表并返回更新成功的数量
     * @param className 实体类全名
     * @param mapList Map列表
     * @return 更新成功的数量
     */
    public int updateBatchMapsCount(String className, List<Map<String, Object>> mapList) {
        try {
            if (mapList == null || mapList.isEmpty()) {
                logger.warn("批量更新Map数据为空，类名: {}", className);
                return 0;
            }

            Class<?> clazz = getClassByFullName(className);
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            
            logger.info("开始批量更新Map数据（返回计数），类名: {}, 数据量: {}", className, mapList.size());
            
            int successCount = 0;
            
            for (Map<String, Object> map : mapList) {
                try {
                    // 将Map转换为实体对象
                    Object entityObj = clazz.getDeclaredConstructor().newInstance();
                    mapToBean(map, entityObj);
                    
                    int result = mapper.updateById(entityObj);
                    if (result > 0) {
                        successCount++;
                    }
                } catch (Exception e) {
                    logger.error("批量更新单条Map数据失败: {}", e.getMessage(), e);
                    // 继续处理其他数据
                }
            }
            
            logger.info("批量更新Map完成，成功更新: {} 条", successCount);
            return successCount;
            
        } catch (Exception e) {
            logger.error("批量更新Map失败，类名: {}", className, e);
            throw new RuntimeException("批量更新Map失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量更新Map列表（通过Class类型）
     * @param clazz 实体类Class
     * @param mapList Map列表
     * @return 是否成功
     */
    public boolean updateBatchMaps(Class<?> clazz, List<Map<String, Object>> mapList) {
        return updateBatchMaps(clazz.getName(), mapList);
    }

    /**
     * 批量更新Map列表并返回更新成功的数量（通过Class类型）
     * @param clazz 实体类Class
     * @param mapList Map列表
     * @return 更新成功的数量
     */
    public int updateBatchMapsCount(Class<?> clazz, List<Map<String, Object>> mapList) {
        return updateBatchMapsCount(clazz.getName(), mapList);
    }

    /**
     * 动态saveOrUpdate方法 - 参照TneWeeklyM3ServiceImpl实现
     * @param className 实体类全名
     * @param entity 实体对象
     * @param updateWrapper 更新条件
     * @return 是否成功
     */
    @SuppressWarnings("unchecked")
    public boolean saveOrUpdate(String className, Object entity, Wrapper updateWrapper) {
        try {
            // 参数校验
            if (entity == null) {
                log.warn("saveOrUpdate实体对象为null，类名: {}", className);
                return false;
            }
            
            // 检查是否误传了List对象
            if (entity instanceof List) {
                log.error("saveOrUpdate不能传入List对象，请传入单个实体对象，类名: {}", className);
                throw new IllegalArgumentException("saveOrUpdate方法不能传入List对象，请使用saveBatch方法处理List数据");
            }
            
            Class<?> clazz = getClassByFullName(className);
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            
            // 处理Map类型转换
            Object processedEntity = entity;
            if (entity instanceof Map) {
                processedEntity = clazz.getDeclaredConstructor().newInstance();
                mapToBean((Map<String, Object>) entity, processedEntity);
            }
            
            // 设置更新时间（如果实体有updateTime字段）
            setUpdateTimeIfExists(processedEntity);
            
            // 尝试根据条件更新
            Boolean updateFlag = false;
            if (updateWrapper != null) {
                int updateResult = mapper.update(processedEntity, updateWrapper);
                if (updateResult > 0) {
                    updateFlag = true;
                    log.info("动态条件更新成功，类名: {}, 影响行数: {}", className, updateResult);
                }
            }
            
            // 如果条件更新失败，则执行saveOrUpdate
            if (!updateFlag) {
                log.debug("条件更新未匹配到记录，执行saveOrUpdate，类名: {}", className);
                
                // 尝试通过Service执行saveOrUpdate
                try {
                    IService<Object> service = (IService<Object>) getServiceByClass(clazz);
                    boolean serviceResult = service.saveOrUpdate(processedEntity);
                    log.info("动态Service saveOrUpdate完成，类名: {}, 结果: {}", className, serviceResult);
                    return serviceResult;
                } catch (Exception serviceEx) {
                    log.warn("Service saveOrUpdate失败，尝试Mapper方式，类名: {}, 错误: {}", className, serviceEx.getMessage());
                    
                    // 如果Service不可用，使用Mapper的逻辑
                    try {
                        Serializable id = extractEntityId(processedEntity);
                        if (id != null) {
                            Object existing = mapper.selectById(id);
                            if (existing != null) {
                                return mapper.updateById(processedEntity) > 0;
                            }
                        }
                        return mapper.insert(processedEntity) > 0;
                    } catch (Exception mapperEx) {
                        log.error("Mapper操作也失败，类名: {}, 错误: {}", className, mapperEx.getMessage());
                        throw mapperEx;
                    }
                }
            }
            
            return updateFlag;
            
        } catch (Exception e) {
            log.error("动态saveOrUpdate失败，类名: {}, entity类型: {}, 错误: {}", 
                    className, entity != null ? entity.getClass().getSimpleName() : "null", e.getMessage(), e);
            throw new RuntimeException("动态saveOrUpdate失败: " + e.getMessage(), e);
        }
    }

    /**
     * 动态saveOrUpdate方法（通过Class类型）
     * @param clazz 实体类Class
     * @param entity 实体对象
     * @param updateWrapper 更新条件
     * @return 是否成功
     */
    public boolean saveOrUpdate(Class<?> clazz, Object entity, Wrapper updateWrapper) {
        return saveOrUpdate(clazz.getName(), entity, updateWrapper);
    }

    /**
     * 通过Service执行saveOrUpdate
     * @param className 实体类全名
     * @param entity 实体对象
     * @param updateWrapper 更新条件
     * @return 是否成功
     */
    @SuppressWarnings("unchecked")
    public boolean saveOrUpdateByService(String className, Object entity, Wrapper updateWrapper) {
        try {
            Class<?> clazz = getClassByFullName(className);
            IService<Object> service = (IService<Object>) getServiceByClass(clazz);
            
            // 处理Map类型转换
            Object processedEntity = entity;
            if (entity instanceof Map) {
                processedEntity = clazz.getDeclaredConstructor().newInstance();
                mapToBean((Map<String, Object>) entity, processedEntity);
            }
            
            // 设置更新时间
            setUpdateTimeIfExists(processedEntity);
            
            logger.info("开始Service saveOrUpdate，类名: {}", className);
            
            // 先尝试条件更新（如果有Service支持的话，否则直接saveOrUpdate）
            boolean result = service.saveOrUpdate(processedEntity);
            
            logger.info("Service saveOrUpdate完成，类名: {}, 结果: {}", className, result);
            return result;
            
        } catch (Exception e) {
            logger.error("Service saveOrUpdate失败，类名: {}", className, e);
            throw new RuntimeException("Service saveOrUpdate失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过Service执行saveOrUpdate（通过Class类型）
     * @param clazz 实体类Class
     * @param entity 实体对象
     * @param updateWrapper 更新条件
     * @return 是否成功
     */
    public boolean saveOrUpdateByService(Class<?> clazz, Object entity, Wrapper updateWrapper) {
        return saveOrUpdateByService(clazz.getName(), entity, updateWrapper);
    }

    /**
     * 基于Map动态构造UpdateWrapper
     * @param conditions 条件Map，key为字段名，value为字段值
     * @return UpdateWrapper实例
     */
    public UpdateWrapper<Object> createUpdateWrapper(Map<String, Object> conditions) {
        UpdateWrapper<Object> wrapper = new UpdateWrapper<>();
        
        if (conditions != null && !conditions.isEmpty()) {
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();
                
                if (value != null) {
                    // 转换驼峰命名为下划线命名（数据库字段格式）
                    String columnName = camelToUnderscore(fieldName);
                    wrapper.eq(columnName, value);
                    logger.debug("添加更新条件: {} = {}", columnName, value);
                }
            }
        }
        
        return wrapper;
    }

    /**
     * 基于实体对象指定字段动态构造UpdateWrapper
     * @param entity 实体对象
     * @param fieldNames 要作为条件的字段名数组
     * @return UpdateWrapper实例
     */
    public UpdateWrapper<Object> createUpdateWrapper(Object entity, String... fieldNames) {
        UpdateWrapper<Object> wrapper = new UpdateWrapper<>();
        
        if (entity != null && fieldNames != null && fieldNames.length > 0) {
            for (String fieldName : fieldNames) {
                try {
                    Object value = getFieldValue(entity, fieldName);
                    if (value != null) {
                        String columnName = camelToUnderscore(fieldName);
                        wrapper.eq(columnName, value);
                        logger.debug("添加更新条件: {} = {}", columnName, value);
                    }
                } catch (Exception e) {
                    logger.warn("获取字段 {} 的值失败: {}", fieldName, e.getMessage());
                }
            }
        }
        
        return wrapper;
    }

    /**
     * 自动识别实体关键字段构造UpdateWrapper（常用唯一标识字段）
     * @param entity 实体对象
     * @return UpdateWrapper实例
     */
    public UpdateWrapper<Object> createAutoUpdateWrapper(Object entity) {
        // 常用的唯一标识字段名
        String[] commonUniqueFields = {
            "sortOrder", "reportLegacyKey", "employeeId", "voucherText", 
            "reportId", "code", "name", "key", "uniqueKey"
        };
        
        UpdateWrapper<Object> wrapper = new UpdateWrapper<>();
        int conditionCount = 0;
        
        if (entity != null) {
            for (String fieldName : commonUniqueFields) {
                try {
                    Object value = getFieldValue(entity, fieldName);
                    if (value != null) {
                        String columnName = camelToUnderscore(fieldName);
                        wrapper.eq(columnName, value);
                        conditionCount++;
                        logger.debug("自动添加更新条件: {} = {}", columnName, value);
                    }
                } catch (Exception e) {
                    // 字段不存在，跳过
                }
            }
        }
        
        logger.debug("自动构造UpdateWrapper完成，条件数量: {}", conditionCount);
        return wrapper;
    }

    /**
     * 通过反射获取对象字段值
     * @param obj 目标对象
     * @param fieldName 字段名
     * @return 字段值
     */
    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Class<?> clazz = obj.getClass();
            Field field = null;

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
            }

            log.warn("未找到字段: {} 在类 {}", fieldName, clazz.getSimpleName());
            return null;
        } catch (Exception e) {
            log.error("获取字段值失败: fieldName={}, error={}", fieldName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 动态saveOrUpdate - 基于Map条件
     * @param className 实体类全名
     * @param entity 实体对象
     * @param conditions 更新条件Map
     * @return 是否成功
     */
    public boolean saveOrUpdate(String className, Object entity, Map<String, Object> conditions) {
        UpdateWrapper<Object> wrapper = createUpdateWrapper(conditions);
        return saveOrUpdate(className, entity, wrapper);
    }

    /**
     * 动态saveOrUpdate - 基于指定字段
     * @param className 实体类全名
     * @param entity 实体对象
     * @param fieldNames 作为条件的字段名
     * @return 是否成功
     */
    public boolean saveOrUpdate(String className, Object entity, String... fieldNames) {
        UpdateWrapper<Object> wrapper = createUpdateWrapper(entity, fieldNames);
        return saveOrUpdate(className, entity, wrapper);
    }

    /**
     * 动态saveOrUpdate - 自动识别条件字段
     * @param className 实体类全名
     * @param entity 实体对象
     * @return 是否成功
     */
    public boolean saveOrUpdateAuto(String className, Object entity) {
        UpdateWrapper<Object> wrapper = createAutoUpdateWrapper(entity);
        return saveOrUpdate(className, entity, wrapper);
    }

    /**
     * Class版本 - 基于Map条件
     */
    public boolean saveOrUpdate(Class<?> clazz, Object entity, Map<String, Object> conditions) {
        return saveOrUpdate(clazz.getName(), entity, conditions);
    }

    /**
     * Class版本 - 基于指定字段
     */
    public boolean saveOrUpdate(Class<?> clazz, Object entity, String... fieldNames) {
        return saveOrUpdate(clazz.getName(), entity, fieldNames);
    }

    /**
     * Class版本 - 自动识别条件字段
     */
    public boolean saveOrUpdateAuto(Class<?> clazz, Object entity) {
        return saveOrUpdateAuto(clazz.getName(), entity);
    }

    /**
     * 批量saveOrUpdate - 处理List数据
     * @param className 实体类全名
     * @param entityList 实体列表
     * @param fieldNames 作为条件的字段名
     * @return 成功处理的数量
     */
    public int saveOrUpdateBatch(String className, List<?> entityList, String... fieldNames) {
        if (entityList == null || entityList.isEmpty()) {
            log.warn("批量saveOrUpdate数据为空，类名: {}", className);
            return 0;
        }

        int successCount = 0;
        log.info("开始批量saveOrUpdate，类名: {}, 数据量: {}", className, entityList.size());

        for (Object entity : entityList) {
            try {
                boolean result;
                if (fieldNames != null && fieldNames.length > 0) {
                    result = saveOrUpdate(className, entity, fieldNames);
                } else {
                    result = saveOrUpdateAuto(className, entity);
                }
                
                if (result) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量saveOrUpdate单条数据失败，类名: {}, 错误: {}", className, e.getMessage());
                // 继续处理其他数据
            }
        }

        log.info("批量saveOrUpdate完成，类名: {}, 成功: {}/{}", className, successCount, entityList.size());
        return successCount;
    }

    /**
     * 批量saveOrUpdate - Class版本
     */
    public int saveOrUpdateBatch(Class<?> clazz, List<?> entityList, String... fieldNames) {
        return saveOrUpdateBatch(clazz.getName(), entityList, fieldNames);
    }

    /**
     * 批量saveOrUpdate - 使用Map条件
     */
    public int saveOrUpdateBatch(String className, List<?> entityList, Map<String, Object> conditions) {
        if (entityList == null || entityList.isEmpty()) {
            log.warn("批量saveOrUpdate数据为空，类名: {}", className);
            return 0;
        }

        int successCount = 0;
        log.info("开始批量saveOrUpdate（Map条件），类名: {}, 数据量: {}", className, entityList.size());

        for (Object entity : entityList) {
            try {
                boolean result = saveOrUpdate(className, entity, conditions);
                if (result) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量saveOrUpdate单条数据失败，类名: {}, 错误: {}", className, e.getMessage());
            }
        }

        log.info("批量saveOrUpdate完成，类名: {}, 成功: {}/{}", className, successCount, entityList.size());
        return successCount;
    }

    /**
     * 批量保存数据
     * @param className 实体类全名
     * @param entityList 实体列表
     * @return 是否成功
     */
    public boolean saveBatch(String className, List<?> entityList) {
        try {
            if (entityList == null || entityList.isEmpty()) {
                logger.warn("批量保存数据为空，类名: {}", className);
                return true;
            }

            Class<?> clazz = getClassByFullName(className);
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            
            logger.info("开始批量保存数据，类名: {}, 数据量: {}", className, entityList.size());
            
            // 批量插入，每次最多1000条
            int batchSize = 1000;
            int totalSize = entityList.size();
            int successCount = 0;
            
            for (int i = 0; i < totalSize; i += batchSize) {
                int endIndex = Math.min(i + batchSize, totalSize);
                List<?> batch = entityList.subList(i, endIndex);
                
                for (Object entity : batch) {
                    try {
                        // 处理Map类型转换
                        Object processedEntity = entity;
                        if (entity instanceof Map) {
                            processedEntity = clazz.getDeclaredConstructor().newInstance();
                            mapToBean((Map<String, Object>) entity, processedEntity);
                        }
                        
                        int result = mapper.insert(processedEntity);
                        if (result > 0) {
                            successCount++;
                        }
                    } catch (Exception e) {
                        logger.error("批量保存单条数据失败: {}", e.getMessage(), e);
                        // 继续处理其他数据，不中断整个批次
                    }
                }
                
                logger.debug("批量保存进度: {}/{}", Math.min(endIndex, totalSize), totalSize);
            }
            
            logger.info("批量保存完成，成功: {}/{}", successCount, totalSize);
            return successCount > 0; // 只要有成功的就返回true
            
        } catch (Exception e) {
            logger.error("批量保存失败，类名: {}", className, e);
            throw new RuntimeException("批量保存失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量保存数据（通过Class类型）
     * @param clazz 实体类Class
     * @param entityList 实体列表
     * @return 是否成功
     */
    public boolean saveBatch(Class<?> clazz, List<?> entityList) {
        return saveBatch(clazz.getName(), entityList);
    }

    /**
     * 通过Service批量保存数据
     * @param className 实体类全名
     * @param entityList 实体列表
     * @return 是否成功
     */
    @SuppressWarnings("unchecked")
    public boolean saveBatchByService(String className, List<?> entityList) {
        try {
            if (entityList == null || entityList.isEmpty()) {
                logger.warn("Service批量保存数据为空，类名: {}", className);
                return true;
            }

            Class<?> clazz = getClassByFullName(className);
            IService<Object> service = (IService<Object>) getServiceByClass(clazz);
            
            logger.info("开始Service批量保存数据，类名: {}, 数据量: {}", className, entityList.size());
            
            // 处理数据转换
            List<Object> processedList = new ArrayList<>();
            for (Object entity : entityList) {
                if (entity instanceof Map) {
                    Object processedEntity = clazz.getDeclaredConstructor().newInstance();
                    mapToBean((Map<String, Object>) entity, processedEntity);
                    processedList.add(processedEntity);
                } else {
                    processedList.add(entity);
                }
            }
            
            // 使用Service的saveBatch方法
            boolean result = service.saveBatch(processedList);
            
            logger.info("Service批量保存完成，结果: {}", result ? "成功" : "失败");
            return result;
            
        } catch (Exception e) {
            logger.error("Service批量保存失败，类名: {}", className, e);
            throw new RuntimeException("Service批量保存失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过Service批量保存数据（通过Class类型）
     * @param clazz 实体类Class
     * @param entityList 实体列表
     * @return 是否成功
     */
    public boolean saveBatchByService(Class<?> clazz, List<?> entityList) {
        return saveBatchByService(clazz.getName(), entityList);
    }

    /**
     * 获取所有已缓存的实体类型
     * @return 实体类型集合
     */
    public Set<Class<?>> getCachedEntityClasses() {
        Set<Class<?>> allClasses = new HashSet<>();
        allClasses.addAll(entityMapperCache.keySet());
        allClasses.addAll(entityServiceCache.keySet());
        return allClasses;
    }

    /**
     * 获取Mapper缓存信息
     * @return Mapper缓存映射
     */
    public Map<String, String> getMapperCacheInfo() {
        Map<String, String> info = new HashMap<>();
        entityMapperCache.forEach((entityClass, mapper) -> 
            info.put(entityClass.getSimpleName(), mapper.getClass().getSimpleName()));
        return info;
    }

    /**
     * 获取Service缓存信息
     * @return Service缓存映射
     */
    public Map<String, String> getServiceCacheInfo() {
        Map<String, String> info = new HashMap<>();
        entityServiceCache.forEach((entityClass, service) -> 
            info.put(entityClass.getSimpleName(), service.getClass().getSimpleName()));
        return info;
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

    /**
     * 检查记录是否存在
     * @param clazz 实体类Class
     * @param queryWrapper 查询条件
     * @return 是否存在
     */
    @SuppressWarnings("unchecked")
    public boolean isRecordExists(Class<?> clazz, Wrapper queryWrapper) {
        try {
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            Long count = mapper.selectCount(queryWrapper);
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("检查记录是否存在失败，类名: {}, 错误: {}", clazz.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * 检查记录是否存在（字符串类名版本）
     * @param className 实体类全名
     * @param queryWrapper 查询条件
     * @return 是否存在
     */
    public boolean isRecordExists(String className, Wrapper queryWrapper) {
        try {
            Class<?> clazz = getClassByFullName(className);
            return isRecordExists(clazz, queryWrapper);
        } catch (Exception e) {
            log.warn("检查记录是否存在失败，类名: {}, 错误: {}", className, e.getMessage());
            return false;
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

    /**
     * 获取字符串类型主键（通用转换）
     */
    public <T> String insertWithStringId(T entity) {
        Serializable id = insertEntityReturnId(entity);
        return convertIdToString(id);
    }

    /**
     * 获取Long类型主键（类型安全，直接传入entity）
     */
    public <T> Long insertWithLongId(T entity) {
        Serializable id = insertEntityReturnId(entity);
        if (id instanceof Long) {
            return (Long) id;
        }
        throw new TypeMismatchException("ID_TYPE_MISMATCH",
                "期望Long类型主键，实际类型: " + id.getClass().getSimpleName());
    }

    /**
     * 批量插入实体列表（直接传入List<Entity>）
     * @param entityList 实体列表
     * @return 是否成功
     */
    public <T> boolean insertBatch(List<T> entityList) {
        try {
            if (entityList == null || entityList.isEmpty()) {
                logger.warn("批量插入数据为空");
                return true;
            }

            // 从第一个实体获取类型信息
            T firstEntity = entityList.get(0);
            if (firstEntity instanceof Map) {
                throw new IllegalArgumentException("此方法不支持Map类型参数，请使用saveBatch方法");
            }

            Class<?> clazz = firstEntity.getClass();
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            
            logger.info("开始批量插入实体对象，类型: {}, 数据量: {}", clazz.getName(), entityList.size());
            
            // 批量插入，每次最多1000条
            int batchSize = 1000;
            int totalSize = entityList.size();
            int successCount = 0;
            
            for (int i = 0; i < totalSize; i += batchSize) {
                int endIndex = Math.min(i + batchSize, totalSize);
                List<T> batch = entityList.subList(i, endIndex);
                
                for (T entity : batch) {
                    try {
                        // 验证实体类型一致性
                        if (!clazz.isInstance(entity)) {
                            logger.warn("跳过类型不匹配的实体，期望: {}, 实际: {}", 
                                clazz.getName(), entity.getClass().getName());
                            continue;
                        }
                        
                        int result = mapper.insert(entity);
                        if (result > 0) {
                            successCount++;
                        }
                    } catch (Exception e) {
                        logger.error("批量插入单条数据失败: {}", e.getMessage(), e);
                        // 继续处理其他数据，不中断整个批次
                    }
                }
                
                logger.debug("批量插入进度: {}/{}", Math.min(endIndex, totalSize), totalSize);
            }
            
            logger.info("批量插入完成，成功: {}/{}", successCount, totalSize);
            return successCount > 0; // 只要有成功的就返回true
            
        } catch (Exception e) {
            logger.error("批量插入失败", e);
            throw new RuntimeException("批量插入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量插入实体列表并返回插入成功的数量
     * @param entityList 实体列表
     * @return 插入成功的数量
     */
    public <T> int insertBatchCount(List<T> entityList) {
        try {
            if (entityList == null || entityList.isEmpty()) {
                logger.warn("批量插入数据为空");
                return 0;
            }

            // 从第一个实体获取类型信息
            T firstEntity = entityList.get(0);
            if (firstEntity instanceof Map) {
                throw new IllegalArgumentException("此方法不支持Map类型参数，请使用saveBatch方法");
            }

            Class<?> clazz = firstEntity.getClass();
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            
            logger.info("开始批量插入实体对象（返回计数），类型: {}, 数据量: {}", clazz.getName(), entityList.size());
            
            int successCount = 0;
            
            for (T entity : entityList) {
                try {
                    // 验证实体类型一致性
                    if (!clazz.isInstance(entity)) {
                        logger.warn("跳过类型不匹配的实体，期望: {}, 实际: {}", 
                            clazz.getName(), entity.getClass().getName());
                        continue;
                    }
                    
                    int result = mapper.insert(entity);
                    if (result > 0) {
                        successCount++;
                    }
                } catch (Exception e) {
                    logger.error("批量插入单条数据失败: {}", e.getMessage(), e);
                    // 继续处理其他数据
                }
            }
            
            logger.info("批量插入完成，成功插入: {} 条", successCount);
            return successCount;
            
        } catch (Exception e) {
            logger.error("批量插入失败", e);
            throw new RuntimeException("批量插入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量插入实体列表并返回所有插入成功的主键ID列表
     * @param entityList 实体列表
     * @return 插入成功的主键ID列表
     */
    public <T> List<Serializable> insertBatchReturnIds(List<T> entityList) {
        try {
            List<Serializable> idList = new ArrayList<>();
            
            if (entityList == null || entityList.isEmpty()) {
                logger.warn("批量插入数据为空");
                return idList;
            }

            // 从第一个实体获取类型信息
            T firstEntity = entityList.get(0);
            if (firstEntity instanceof Map) {
                throw new IllegalArgumentException("此方法不支持Map类型参数，请使用saveBatch方法");
            }

            Class<?> clazz = firstEntity.getClass();
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            Field idField = getIdField(clazz);
            
            logger.info("开始批量插入实体对象（返回ID列表），类型: {}, 数据量: {}", clazz.getName(), entityList.size());
            
            for (T entity : entityList) {
                try {
                    // 验证实体类型一致性
                    if (!clazz.isInstance(entity)) {
                        logger.warn("跳过类型不匹配的实体，期望: {}, 实际: {}", 
                            clazz.getName(), entity.getClass().getName());
                        continue;
                    }
                    
                    int result = mapper.insert(entity);
                    if (result > 0) {
                        // 获取插入后的主键值
                        idField.setAccessible(true);
                        Object idValue = idField.get(entity);
                        if (idValue instanceof Serializable) {
                            idList.add((Serializable) idValue);
                        }
                    }
                } catch (Exception e) {
                    logger.error("批量插入单条数据失败: {}", e.getMessage(), e);
                    // 继续处理其他数据
                }
            }
            
            logger.info("批量插入完成，成功插入: {} 条，返回ID数量: {}", idList.size(), idList.size());
            return idList;
            
        } catch (Exception e) {
            logger.error("批量插入失败", e);
            throw new RuntimeException("批量插入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量插入Map列表（需要指定实体类名）
     * @param className 实体类全名
     * @param mapList Map列表
     * @return 是否成功
     */
    public boolean insertBatchMaps(String className, List<Map<String, Object>> mapList) {
        try {
            if (mapList == null || mapList.isEmpty()) {
                logger.warn("批量插入Map数据为空，类名: {}", className);
                return true;
            }

            Class<?> clazz = getClassByFullName(className);
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            
            logger.info("开始批量插入Map数据，类名: {}, 数据量: {}", className, mapList.size());
            
            // 批量插入，每次最多1000条
            int batchSize = 1000;
            int totalSize = mapList.size();
            int successCount = 0;
            
            for (int i = 0; i < totalSize; i += batchSize) {
                int endIndex = Math.min(i + batchSize, totalSize);
                List<Map<String, Object>> batch = mapList.subList(i, endIndex);
                
                for (Map<String, Object> map : batch) {
                    try {
                        // 将Map转换为实体对象
                        Object entityObj = clazz.getDeclaredConstructor().newInstance();
                        mapToBean(map, entityObj);
                        
                        int result = mapper.insert(entityObj);
                        if (result > 0) {
                            successCount++;
                        }
                    } catch (Exception e) {
                        logger.error("批量插入单条Map数据失败: {}", e.getMessage(), e);
                        // 继续处理其他数据，不中断整个批次
                    }
                }
                
                logger.debug("批量插入Map进度: {}/{}", Math.min(endIndex, totalSize), totalSize);
            }
            
            logger.info("批量插入Map完成，成功: {}/{}", successCount, totalSize);
            return successCount > 0; // 只要有成功的就返回true
            
        } catch (Exception e) {
            logger.error("批量插入Map失败，类名: {}", className, e);
            throw new RuntimeException("批量插入Map失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量插入Map列表并返回插入成功的数量
     * @param className 实体类全名
     * @param mapList Map列表
     * @return 插入成功的数量
     */
    public int insertBatchMapsCount(String className, List<Map<String, Object>> mapList) {
        try {
            if (mapList == null || mapList.isEmpty()) {
                logger.warn("批量插入Map数据为空，类名: {}", className);
                return 0;
            }

            Class<?> clazz = getClassByFullName(className);
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            
            logger.info("开始批量插入Map数据（返回计数），类名: {}, 数据量: {}", className, mapList.size());
            
            int successCount = 0;
            
            for (Map<String, Object> map : mapList) {
                try {
                    // 将Map转换为实体对象
                    Object entityObj = clazz.getDeclaredConstructor().newInstance();
                    mapToBean(map, entityObj);
                    
                    int result = mapper.insert(entityObj);
                    if (result > 0) {
                        successCount++;
                    }
                } catch (Exception e) {
                    logger.error("批量插入单条Map数据失败: {}", e.getMessage(), e);
                    // 继续处理其他数据
                }
            }
            
            logger.info("批量插入Map完成，成功插入: {} 条", successCount);
            return successCount;
            
        } catch (Exception e) {
            logger.error("批量插入Map失败，类名: {}", className, e);
            throw new RuntimeException("批量插入Map失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量插入Map列表并返回所有插入成功的主键ID列表
     * @param className 实体类全名
     * @param mapList Map列表
     * @return 插入成功的主键ID列表
     */
    public List<Serializable> insertBatchMapsReturnIds(String className, List<Map<String, Object>> mapList) {
        try {
            List<Serializable> idList = new ArrayList<>();
            
            if (mapList == null || mapList.isEmpty()) {
                logger.warn("批量插入Map数据为空，类名: {}", className);
                return idList;
            }

            Class<?> clazz = getClassByFullName(className);
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            Field idField = getIdField(clazz);
            
            logger.info("开始批量插入Map数据（返回ID列表），类名: {}, 数据量: {}", className, mapList.size());
            
            for (Map<String, Object> map : mapList) {
                try {
                    // 将Map转换为实体对象
                    Object entityObj = clazz.getDeclaredConstructor().newInstance();
                    mapToBean(map, entityObj);
                    
                    int result = mapper.insert(entityObj);
                    if (result > 0) {
                        // 获取插入后的主键值
                        idField.setAccessible(true);
                        Object idValue = idField.get(entityObj);
                        if (idValue instanceof Serializable) {
                            idList.add((Serializable) idValue);
                        }
                    }
                } catch (Exception e) {
                    logger.error("批量插入单条Map数据失败: {}", e.getMessage(), e);
                    // 继续处理其他数据
                }
            }
            
            logger.info("批量插入Map完成，成功插入: {} 条，返回ID数量: {}", idList.size(), idList.size());
            return idList;
            
        } catch (Exception e) {
            logger.error("批量插入Map失败，类名: {}", className, e);
            throw new RuntimeException("批量插入Map失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量插入Map列表（通过Class类型）
     * @param clazz 实体类Class
     * @param mapList Map列表
     * @return 是否成功
     */
    public boolean insertBatchMaps(Class<?> clazz, List<Map<String, Object>> mapList) {
        return insertBatchMaps(clazz.getName(), mapList);
    }

    /**
     * 批量插入Map列表并返回插入成功的数量（通过Class类型）
     * @param clazz 实体类Class
     * @param mapList Map列表
     * @return 插入成功的数量
     */
    public int insertBatchMapsCount(Class<?> clazz, List<Map<String, Object>> mapList) {
        return insertBatchMapsCount(clazz.getName(), mapList);
    }

    /**
     * 批量插入Map列表并返回所有插入成功的主键ID列表（通过Class类型）
     * @param clazz 实体类Class
     * @param mapList Map列表
     * @return 插入成功的主键ID列表
     */
    public List<Serializable> insertBatchMapsReturnIds(Class<?> clazz, List<Map<String, Object>> mapList) {
        return insertBatchMapsReturnIds(clazz.getName(), mapList);
    }

    /******************** 核心私有方法 ********************/
    private void initializeMapperCache() {
        applicationContext.getBeansOfType(BaseMapper.class).values().forEach(mapper -> {
            Class<?> entityClass = getEntityClass(mapper);
            entityMapperCache.put(entityClass, mapper);
            cacheIdField(entityClass); // 预缓存ID字段
        });
    }

    private void initializeServiceCache() {
        Map<String, IService> serviceMap = applicationContext.getBeansOfType(IService.class);
        logger.info("发现 {} 个IService实例", serviceMap.size());
        
        serviceMap.values().forEach(service -> {
            try {
                logger.debug("尝试解析Service: {}", service.getClass().getName());
                Class<?> entityClass = resolveServiceEntityClass(service);
                if (entityClass != null) {
                    entityServiceCache.put(entityClass, service);
                    logger.info("成功缓存Service: {} for entity: {}", 
                              service.getClass().getSimpleName(), entityClass.getSimpleName());
                } else {
                    logger.warn("无法解析实体类，Service: {}", service.getClass().getName());
                    // 尝试特殊处理已知的Service
                    if (service.getClass().getSimpleName().contains("TneMonthlyExpenseAccrualReportServiceImpl")) {
                        try {
                            Class<?> specialEntityClass = Class.forName("org.jeecg.modules.tne.entity.TneMonthlyExpenseAccrualReport");
                            entityServiceCache.put(specialEntityClass, service);
                            logger.info("特殊处理成功缓存Service: {} for entity: {}", 
                                      service.getClass().getSimpleName(), specialEntityClass.getSimpleName());
                        } catch (ClassNotFoundException e) {
                            logger.error("特殊处理失败，找不到实体类: {}", e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("缓存Service失败: {} - {}", service.getClass().getName(), e.getMessage());
            }
        });
        
        logger.info("Service缓存初始化完成，共缓存 {} 个Service", entityServiceCache.size());
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

    private Class<?> resolveServiceEntityClass(IService<?> service) {
        Class<?> serviceClass = service.getClass();
        logger.debug("开始解析Service实体类: {}", serviceClass.getName());
        
        // 方案1：检查是否是CGLIB代理类
        if (serviceClass.getName().contains("$$")) {
            serviceClass = serviceClass.getSuperclass();
            logger.debug("检测到CGLIB代理，使用父类: {}", serviceClass.getName());
        }
        
        // 方案2：遍历类继承层次
        while (serviceClass != null && serviceClass != Object.class) {
            logger.debug("检查类: {}", serviceClass.getName());
            
            // 检查父类的泛型信息（ServiceImpl模式）
            Type superclass = serviceClass.getGenericSuperclass();
            if (superclass instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) superclass;
                logger.debug("父类泛型类型: {}", pType.toString());
                Type[] typeArgs = pType.getActualTypeArguments();
                if (typeArgs.length >= 2 && typeArgs[1] instanceof Class) {
                    // ServiceImpl<Mapper, Entity> - Entity是第二个参数
                    Class<?> entityClass = (Class<?>) typeArgs[1];
                    logger.debug("从父类泛型中解析到实体类: {}", entityClass.getName());
                    return entityClass;
                }
            }
            
            // 检查实现的接口
            Type[] interfaces = serviceClass.getGenericInterfaces();
            for (Type type : interfaces) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType) type;
                    Type rawType = pType.getRawType();
                    
                    // 检查是否实现了IService接口
                    if (rawType instanceof Class && IService.class.isAssignableFrom((Class<?>) rawType)) {
                        Type[] typeArgs = pType.getActualTypeArguments();
                        if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                            Class<?> entityClass = (Class<?>) typeArgs[0];
                            logger.debug("从接口泛型中解析到实体类: {}", entityClass.getName());
                            return entityClass;
                        }
                    }
                }
            }
            
            serviceClass = serviceClass.getSuperclass();
        }
        
        logger.debug("无法解析Service实体类: {}", service.getClass().getName());
        return null;
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
            BeanUtils.populate(instance, (Map<String, ? extends Object>) entity);
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

    /**
     * 替换原来的registerDateConverter方法，更全面地解决日期转换问题
     */
    private void setupBeanUtilsDateConverter() {
        try {
            // 创建一个新的DateConverter实例，传入null作为默认值
            DateConverter converter = new DateConverter(null);
            
            // 设置支持的日期格式
            String[] patterns = {
                "yyyy-MM-dd", 
                "yyyy-MM-dd HH:mm:ss",
                "yyyy/MM/dd", 
                "yyyy/MM/dd HH:mm:ss",
                "MM/dd/yyyy", 
                "dd/MM/yyyy",
                "yyyy.MM.dd", 
                "yyyy.MM.dd HH:mm:ss",
                "yyyyMMdd",
                "yyyyMMddHHmmss"
            };
            
            // 设置日期格式模式
            converter.setPatterns(patterns);
            
            // 注册到ConvertUtils中
            ConvertUtils.register(converter, Date.class);
            ConvertUtils.register(converter, java.sql.Date.class);
            ConvertUtils.register(converter, java.sql.Timestamp.class);
            
            logger.info("已成功配置日期转换器，支持的格式: {}", Arrays.toString(patterns));
        } catch (Exception e) {
            logger.error("配置日期转换器失败", e);
            throw new RuntimeException("配置日期转换器失败", e);
        }
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

    /**
     * 预处理Map中的日期字段
     */
    private Map<String, Object> preprocessDateFields(Map<String, Object> sourceMap, Class<?> targetClass) {
        Map<String, Object> result = new HashMap<>(sourceMap);
        
        // 检查目标类中的每个字段
        for (Field field : getAllFields(targetClass)) {
            String fieldName = field.getName();
            
            // 只处理日期类型的字段
            if (Date.class.isAssignableFrom(field.getType()) && result.containsKey(fieldName)) {
                Object value = result.get(fieldName);
                
                // 如果字段值是字符串，但目标是日期类型，则移除该字段，让BeanUtils的转换器处理
                if (value instanceof String && ((String) value).trim().isEmpty()) {
                    // 对于空字符串，直接设置为null
                    result.put(fieldName, null);
                    logger.debug("字段 '{}' 是空字符串，已设置为null", fieldName);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 获取类及其所有父类的所有字段
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;
        
        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }
        
        return fields;
    }

    /**
     * 手动将Map转换为Bean对象，解决日期转换问题
     */
    private void mapToBean(Map<String, Object> map, Object bean) throws Exception {
        Class<?> beanClass = bean.getClass();

        // 获取所有字段（包括父类）
        for (Field field : getAllFields(beanClass)) {
            field.setAccessible(true);
            String fieldName = field.getName();

            // 如果Map中包含该字段
            if (map.containsKey(fieldName)) {
                Object value = map.get(fieldName);

                // 特殊处理日期类型
                if (value != null && (field.getType() == Date.class ||
                        field.getType() == java.sql.Date.class ||
                        field.getType() == java.sql.Timestamp.class)) {
                    if (value instanceof String) {
                        String strValue = (String) value;
                        // 空字符串转为null
                        if (strValue.trim().isEmpty()) {
                            field.set(bean, null);
                        } else {
                            // 尝试解析日期
                            try {
                                Date date = parseDate(strValue);

                                // 根据字段类型转换
                                if (field.getType() == java.sql.Date.class) {
                                    field.set(bean, new java.sql.Date(date.getTime()));
                                } else if (field.getType() == java.sql.Timestamp.class) {
                                    field.set(bean, new java.sql.Timestamp(date.getTime()));
                                } else {
                                    field.set(bean, date);
                                }
                            } catch (Exception e) {
                                logger.warn("无法解析日期字段 '{}' 的值 '{}'，设置为null", fieldName, strValue);
                                field.set(bean, null);
                            }
                        }
                    } else if (value instanceof Date) {
                        // 已经是Date类型，可能需要转换为特定日期类型
                        Date dateValue = (Date) value;
                        if (field.getType() == java.sql.Date.class) {
                            field.set(bean, new java.sql.Date(dateValue.getTime()));
                        } else if (field.getType() == java.sql.Timestamp.class) {
                            field.set(bean, new java.sql.Timestamp(dateValue.getTime()));
                        } else {
                            field.set(bean, dateValue);
                        }
                    } else {
                        logger.warn("字段 '{}' 值类型不兼容，期望Date或String，实际是: {}", fieldName, value.getClass().getName());
                        field.set(bean, null);
                    }
                }
                // 处理其他类型（非日期类型）
                else if (value != null) {
                    try {
                        // 使用BeanUtils转换普通类型
                        Object convertedValue = ConvertUtils.convert(value, field.getType());
                        field.set(bean, convertedValue);
                    } catch (Exception e) {
                        logger.warn("字段 '{}' 值 '{}' 转换失败: {}", fieldName, value, e.getMessage());
                        // 如果转换失败，对象型字段设置为null，基本类型保持默认值
                        if (!field.getType().isPrimitive()) {
                            field.set(bean, null);
                        }
                    }
                } else {
                    // 值为null，对象型字段设置为null，基本类型保持默认值
                    if (!field.getType().isPrimitive()) {
                        field.set(bean, null);
                    }
                }
            }
        }
    }

    /**
     * 驼峰命名转下划线命名
     * @param camelStr 驼峰命名字符串
     * @return 下划线命名字符串
     */
    private String camelToUnderscore(String camelStr) {
        if (camelStr == null || camelStr.isEmpty()) {
            return camelStr;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelStr.length(); i++) {
            char c = camelStr.charAt(i);
            char prevChar = i > 0 ? camelStr.charAt(i - 1) : '\0';
            
            if (Character.isUpperCase(c)) {
                // 如果不是第一个字符，在大写字母前添加下划线
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else if (Character.isDigit(c)) {
                // 处理数字：如果前一个字符是字母，则在数字前添加下划线
                if (i > 0 && Character.isLetter(prevChar)) {
                    result.append('_');
                }
                result.append(c);
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }

    /**
     * 设置updateTime字段（如果实体有该字段）
     * @param entity 实体对象
     */
    private void setUpdateTimeIfExists(Object entity) {
        try {
            Class<?> clazz = entity.getClass();
            
            // 查找updateTime字段
            Field updateTimeField = null;
            try {
                updateTimeField = clazz.getDeclaredField("updateTime");
            } catch (NoSuchFieldException e) {
                // 检查父类
                Class<?> superClass = clazz.getSuperclass();
                while (superClass != null && superClass != Object.class) {
                    try {
                        updateTimeField = superClass.getDeclaredField("updateTime");
                        break;
                    } catch (NoSuchFieldException ex) {
                        superClass = superClass.getSuperclass();
                    }
                }
            }
            
            if (updateTimeField != null) {
                updateTimeField.setAccessible(true);
                // 检查字段类型是否是Date类型
                if (Date.class.isAssignableFrom(updateTimeField.getType())) {
                    updateTimeField.set(entity, DateUtils.getDate());
                    logger.debug("设置updateTime字段成功，类名: {}", clazz.getSimpleName());
                }
            } else {
                logger.debug("未找到updateTime字段，类名: {}", clazz.getSimpleName());
            }
        } catch (Exception e) {
            logger.warn("设置updateTime字段失败: {}", e.getMessage());
        }
    }

    /**
     * 尝试用多种格式解析日期字符串
     */
    private Date parseDate(String dateStr) throws Exception {
        String[] patterns = {
                "yyyy-MM-dd",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy/MM/dd",
                "yyyy/MM/dd HH:mm:ss",
                "MM/dd/yyyy",
                "dd/MM/yyyy",
                "yyyy.MM.dd",
                "yyyy.MM.dd HH:mm:ss",
                "yyyyMMdd",
                "yyyyMMddHHmmss"
        };

        // 移除两端可能的空白字符
        dateStr = dateStr.trim();

        // 尝试不同的日期格式
        for (String pattern : patterns) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pattern);
                sdf.setLenient(false); // 严格模式
                return sdf.parse(dateStr);
            } catch (Exception e) {
                // 继续尝试下一个格式
            }
        }

        // 如果所有格式都失败，抛出异常
        throw new IllegalArgumentException("无法解析日期: " + dateStr);
    }

    /**
     * 根据条件查询单条记录
     * @param className 实体类全名
     * @param queryWrapper 查询条件
     * @return 查询结果
     */
    @SuppressWarnings("unchecked")
    public <T> T selectOne(String className, Wrapper<T> queryWrapper) {
        try {
            Class<?> clazz = getClassByFullName(className);
            BaseMapper<T> mapper = (BaseMapper<T>) getMapperByClass(clazz);
            return mapper.selectOne(queryWrapper);
        } catch (Exception e) {
            logger.error("根据条件查询单条记录失败，类名: {}", className, e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据条件查询单条记录（通过Class类型）
     * @param clazz 实体类Class
     * @param queryWrapper 查询条件
     * @return 查询结果
     */
    public <T> T selectOne(Class<T> clazz, Wrapper<T> queryWrapper) {
        return selectOne(clazz.getName(), queryWrapper);
    }

    /**
     * 根据条件查询记录列表
     * @param className 实体类全名
     * @param queryWrapper 查询条件
     * @return 查询结果列表
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> selectList(String className, Wrapper<T> queryWrapper) {
        try {
            Class<?> clazz = getClassByFullName(className);
            BaseMapper<T> mapper = (BaseMapper<T>) getMapperByClass(clazz);
            return mapper.selectList(queryWrapper);
        } catch (Exception e) {
            logger.error("根据条件查询记录列表失败，类名: {}", className, e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据条件查询记录列表（通过Class类型）
     * @param clazz 实体类Class
     * @param queryWrapper 查询条件
     * @return 查询结果列表
     */
    public <T> List<T> selectList(Class<T> clazz, Wrapper<T> queryWrapper) {
        return selectList(clazz.getName(), queryWrapper);
    }

    /**
     * 根据条件查询记录数量
     * @param className 实体类全名
     * @param queryWrapper 查询条件
     * @return 记录数量
     */
    @SuppressWarnings("unchecked")
    public Long selectCount(String className, Wrapper queryWrapper) {
        try {
            Class<?> clazz = getClassByFullName(className);
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            return mapper.selectCount(queryWrapper);
        } catch (Exception e) {
            logger.error("根据条件查询记录数量失败，类名: {}", className, e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据条件查询记录数量（通过Class类型）
     * @param clazz 实体类Class
     * @param queryWrapper 查询条件
     * @return 记录数量
     */
    public Long selectCount(Class<?> clazz, Wrapper queryWrapper) {
        return selectCount(clazz.getName(), queryWrapper);
    }

    /**
     * 根据条件查询Map列表
     * @param className 实体类全名
     * @param queryWrapper 查询条件
     * @return Map列表
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> selectMaps(String className, Wrapper queryWrapper) {
        try {
            Class<?> clazz = getClassByFullName(className);
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            return mapper.selectMaps(queryWrapper);
        } catch (Exception e) {
            logger.error("根据条件查询Map列表失败，类名: {}", className, e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据条件查询Map列表（通过Class类型）
     * @param clazz 实体类Class
     * @param queryWrapper 查询条件
     * @return Map列表
     */
    public List<Map<String, Object>> selectMaps(Class<?> clazz, Wrapper queryWrapper) {
        return selectMaps(clazz.getName(), queryWrapper);
    }

    /**
     * 分页查询记录列表
     * @param className 实体类全名
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    @SuppressWarnings("unchecked")
    public <T> IPage<T> selectPage(String className, Integer pageNum, Integer pageSize, Wrapper<T> queryWrapper) {
        try {
            Class<?> clazz = getClassByFullName(className);
            BaseMapper<T> mapper = (BaseMapper<T>) getMapperByClass(clazz);
            
            Page<T> page = new Page<>(pageNum, pageSize);
            return mapper.selectPage(page, queryWrapper);
        } catch (Exception e) {
            logger.error("分页查询记录列表失败，类名: {}", className, e);
            throw new RuntimeException("分页查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 分页查询记录列表（通过Class类型）
     * @param clazz 实体类Class
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    public <T> IPage<T> selectPage(Class<T> clazz, Integer pageNum, Integer pageSize, Wrapper<T> queryWrapper) {
        return selectPage(clazz.getName(), pageNum, pageSize, queryWrapper);
    }

    /**
     * 分页查询Map列表
     * @param className 实体类全名
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    @SuppressWarnings("unchecked")
    public IPage<Map<String, Object>> selectMapsPage(String className, Integer pageNum, Integer pageSize, Wrapper queryWrapper) {
        try {
            Class<?> clazz = getClassByFullName(className);
            BaseMapper<Object> mapper = (BaseMapper<Object>) getMapperByClass(clazz);
            
            Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
            return mapper.selectMapsPage(page, queryWrapper);
        } catch (Exception e) {
            logger.error("分页查询Map列表失败，类名: {}", className, e);
            throw new RuntimeException("分页查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 分页查询Map列表（通过Class类型）
     * @param clazz 实体类Class
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    public IPage<Map<String, Object>> selectMapsPage(Class<?> clazz, Integer pageNum, Integer pageSize, Wrapper queryWrapper) {
        return selectMapsPage(clazz.getName(), pageNum, pageSize, queryWrapper);
    }

}