package com.epiroc.workflow.common.system.query;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.epiroc.workflow.common.util.DateUtils;
import com.epiroc.workflow.common.util.oConvertUtils;
import com.epiroc.workflow.common.system.constant.CommonConstant;
import com.epiroc.workflow.common.system.constant.DataBaseConstant;
import com.epiroc.workflow.common.system.exception.WorkflowException;
import com.epiroc.workflow.common.system.vo.SysPermissionDataRuleModel;
import com.epiroc.workflow.common.util.CommonUtils;
import com.epiroc.workflow.common.util.ReflectHelper;
import com.epiroc.workflow.common.util.SqlInjectionUtil;
import com.epiroc.workflow.common.util.WorkflowDataAutorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.util.NumberUtils;

import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class QueryGenerator {

    public static final String SQL_RULES_COLUMN = "SQL_RULES_COLUMN";

    private static final String BEGIN = "_begin";
    private static final String END = "_end";
    /**
     * 数字类型字段，拼接此后缀 接受多值参数
     */
    private static final String MULTI = "_MultiString";
    private static final String STAR = "*";
    private static final String COMMA = ",";
    /**
     * 查询 逗号转义符 相当于一个逗号【作废】
     */
    public static final String QUERY_COMMA_ESCAPE = "++";
    private static final String NOT_EQUAL = "!";
    /**页面带有规则值查询，空格作为分隔符*/
    private static final String QUERY_SEPARATE_KEYWORD = " ";
    /**高级查询前端传来的参数名*/
    private static final String SUPER_QUERY_PARAMS = "superQueryParams";
    /** 高级查询前端传来的拼接方式参数名 */
    private static final String SUPER_QUERY_MATCH_TYPE = "superQueryMatchType";
    /** 单引号 */
    public static final String SQL_SQ = "'";
    /**排序列*/
    private static final String ORDER_COLUMN = "column";
    /**排序方式*/
    private static final String ORDER_TYPE = "order";
    private static final String ORDER_TYPE_ASC = "ASC";

    /**mysql 模糊查询之特殊字符下划线 （_、\）*/
    public static final String LIKE_MYSQL_SPECIAL_STRS = "_,%";

    /**日期格式化yyyy-MM-dd*/
    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    /**to_date*/
    public static final String TO_DATE = "to_date";

    private static final ThreadLocal<SimpleDateFormat> LOCAL = new ThreadLocal();

    public static <T> QueryWrapper<T> initQueryWrapper(T searchObj, Map<String, String[]> parameterMap) {
        long start = System.currentTimeMillis();
        QueryWrapper<T> queryWrapper = new QueryWrapper();
        installMplus(queryWrapper, searchObj, parameterMap);
        log.debug("---查询条件构造器初始化完成,耗时:" + (System.currentTimeMillis() - start) + "毫秒----");
        return queryWrapper;
    }

    /**
     * 组装Mybatis Plus 查询条件
     * <p>使用此方法 需要有如下几点注意:
     * <br>1.使用QueryWrapper 而非LambdaQueryWrapper;
     * <br>2.实例化QueryWrapper时不可将实体传入参数
     * <br>错误示例:如QueryWrapper<JeecgDemo> queryWrapper = new QueryWrapper<JeecgDemo>(jeecgDemo);
     * <br>正确示例:QueryWrapper<JeecgDemo> queryWrapper = new QueryWrapper<JeecgDemo>();
     * <br>3.也可以不使用这个方法直接调用 {@link #initQueryWrapper}直接获取实例
     */
    private static void installMplus(QueryWrapper<?> queryWrapper,Object searchObj,Map<String, String[]> parameterMap) {

		/*
		 * 注意:权限查询由前端配置数据规则 当一个人有多个所属部门时候 可以在规则配置包含条件 orgCode 包含 #{sys_org_code}
		但是不支持在自定义SQL中写orgCode in #{sys_org_code}
		当一个人只有一个部门 就直接配置等于条件: orgCode 等于 #{sys_org_code} 或者配置自定义SQL: orgCode = '#{sys_org_code}'
		*/

        //区间条件组装 模糊查询 高级查询组装 简单排序 权限查询
        PropertyDescriptor[] origDescriptors = PropertyUtils.getPropertyDescriptors(searchObj);
        Map<String, SysPermissionDataRuleModel> ruleMap = getRuleMap();

        //权限规则自定义SQL表达式
        for (String c : ruleMap.keySet()) {
            if(oConvertUtils.isNotEmpty(c) && c.startsWith(SQL_RULES_COLUMN)){
                queryWrapper.and(i ->i.apply(getSqlRuleValue(ruleMap.get(c).getRuleValue())));
            }
        }

        String name, type, column;
        // update-begin--Author:taoyan  Date:20200923 for：issues/1671 如果字段加注解了@TableField(exist = false),不走DB查询-------
        //定义实体字段和数据库字段名称的映射 高级查询中 只能获取实体字段 如果设置TableField注解 那么查询条件会出问题
        Map<String,String> fieldColumnMap = new HashMap<>(5);
        for (int i = 0; i < origDescriptors.length; i++) {
            //aliasName = origDescriptors[i].getName();  mybatis  不存在实体属性 不用处理别名的情况
            name = origDescriptors[i].getName();
            type = origDescriptors[i].getPropertyType().toString();
            try {
                if (judgedIsUselessField(name)|| !PropertyUtils.isReadable(searchObj, name)) {
                    continue;
                }

                Object value = PropertyUtils.getSimpleProperty(searchObj, name);
                column = ReflectHelper.getTableFieldName(searchObj.getClass(), name);
                if(column==null){
                    //column为null只有一种情况 那就是 添加了注解@TableField(exist = false) 后续都不用处理了
                    continue;
                }
                fieldColumnMap.put(name,column);
                //数据权限查询
                if(ruleMap.containsKey(name)) {
                    addRuleToQueryWrapper(ruleMap.get(name), column, origDescriptors[i].getPropertyType(), queryWrapper);
                }
                //区间查询
                doIntervalQuery(queryWrapper, parameterMap, type, name, column);
                //判断单值  参数带不同标识字符串 走不同的查询
                //TODO 这种前后带逗号的支持分割后模糊查询(多选字段查询生效) 示例：,1,3,
                if (null != value && value.toString().startsWith(COMMA) && value.toString().endsWith(COMMA)) {
                    String multiLikeval = value.toString().replace(",,", COMMA);
                    String[] vals = multiLikeval.substring(1, multiLikeval.length()).split(COMMA);
                    final String field = oConvertUtils.camelToUnderline(column);
                    if(vals.length>1) {
                        queryWrapper.and(j -> {
                            log.info("---查询过滤器，Query规则---field:{}, rule:{}, value:{}", field, "like", vals[0]);
                            j = j.like(field,vals[0]);
                            for (int k=1;k<vals.length;k++) {
                                j = j.or().like(field,vals[k]);
                                log.info("---查询过滤器，Query规则 .or()---field:{}, rule:{}, value:{}", field, "like", vals[k]);
                            }
                            //return j;
                        });
                    }else {
                        log.info("---查询过滤器，Query规则---field:{}, rule:{}, value:{}", field, "like", vals[0]);
                        queryWrapper.and(j -> j.like(field,vals[0]));
                    }
                }else {
                    //根据参数值带什么关键字符串判断走什么类型的查询
                    QueryRuleEnum rule = convert2Rule(value);
                    value = replaceValue(rule,value);
                    // add -begin 添加判断为字符串时设为全模糊查询
                    //if( (rule==null || QueryRuleEnum.EQ.equals(rule)) && "class java.lang.String".equals(type)) {
                    // 可以设置左右模糊或全模糊，因人而异
                    //rule = QueryRuleEnum.LIKE;
                    //}
                    // add -end 添加判断为字符串时设为全模糊查询
                    addEasyQuery(queryWrapper, column, rule, value);
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        // 排序逻辑 处理
        doMultiFieldsOrder(queryWrapper, parameterMap, fieldColumnMap);

        //高级查询
        doSuperQuery(queryWrapper, parameterMap, fieldColumnMap);
        // update-end--Author:taoyan  Date:20200923 for：issues/1671 如果字段加注解了@TableField(exist = false),不走DB查询-------

    }

    /**
     * 高级查询
     * @param queryWrapper 查询对象
     * @param parameterMap 参数对象
     * @param fieldColumnMap 实体字段和数据库列对应的map
     */
    private static void doSuperQuery(QueryWrapper<?> queryWrapper,Map<String, String[]> parameterMap, Map<String,String> fieldColumnMap) {
        if(parameterMap!=null&& parameterMap.containsKey(SUPER_QUERY_PARAMS)){
            String superQueryParams = parameterMap.get(SUPER_QUERY_PARAMS)[0];
            String superQueryMatchType = parameterMap.get(SUPER_QUERY_MATCH_TYPE) != null ? parameterMap.get(SUPER_QUERY_MATCH_TYPE)[0] : MatchTypeEnum.AND.getValue();
            MatchTypeEnum matchType = MatchTypeEnum.getByValue(superQueryMatchType);
            // update-begin--Author:sunjianlei  Date:20200325 for：高级查询的条件要用括号括起来，防止和用户的其他条件冲突 -------
            try {
                superQueryParams = URLDecoder.decode(superQueryParams, "UTF-8");
                List<QueryCondition> conditions = JSON.parseArray(superQueryParams, QueryCondition.class);
                if (conditions == null || conditions.size() == 0) {
                    return;
                }
                // update-begin-author:sunjianlei date:20220119 for: 【JTC-573】 过滤空条件查询，防止 sql 拼接多余的 and
                List<QueryCondition> filterConditions = conditions.stream().filter(
                        rule -> oConvertUtils.isNotEmpty(rule.getField())
                                && oConvertUtils.isNotEmpty(rule.getRule())
                                && oConvertUtils.isNotEmpty(rule.getVal())
                ).collect(Collectors.toList());
                if (filterConditions.size() == 0) {
                    return;
                }
                // update-end-author:sunjianlei date:20220119 for: 【JTC-573】 过滤空条件查询，防止 sql 拼接多余的 and
                log.debug("---高级查询参数-->" + filterConditions);

                queryWrapper.and(andWrapper -> {
                    for (int i = 0; i < filterConditions.size(); i++) {
                        QueryCondition rule = filterConditions.get(i);
                        if (oConvertUtils.isNotEmpty(rule.getField())
                                && oConvertUtils.isNotEmpty(rule.getRule())
                                && oConvertUtils.isNotEmpty(rule.getVal())) {

                            log.debug("SuperQuery ==> " + rule.toString());

                            //update-begin-author:taoyan date:20201228 for: 【高级查询】 oracle 日期等于查询报错
                            Object queryValue = rule.getVal();
                            if("date".equals(rule.getType())){
                                queryValue = DateUtils.str2Date(rule.getVal(),DateUtils.date_sdf.get());
                            }else if("datetime".equals(rule.getType())){
                                queryValue = DateUtils.str2Date(rule.getVal(), DateUtils.datetimeFormat.get());
                            }
                            // update-begin--author:sunjianlei date:20210702 for：【/issues/I3VR8E】高级查询没有类型转换，查询参数都是字符串类型 ----
                            String dbType = rule.getDbType();
                            if (oConvertUtils.isNotEmpty(dbType)) {
                                try {
                                    String valueStr = String.valueOf(queryValue);
                                    switch (dbType.toLowerCase().trim()) {
                                        case "int":
                                            queryValue = Integer.parseInt(valueStr);
                                            break;
                                        case "bigdecimal":
                                            queryValue = new BigDecimal(valueStr);
                                            break;
                                        case "short":
                                            queryValue = Short.parseShort(valueStr);
                                            break;
                                        case "long":
                                            queryValue = Long.parseLong(valueStr);
                                            break;
                                        case "float":
                                            queryValue = Float.parseFloat(valueStr);
                                            break;
                                        case "double":
                                            queryValue = Double.parseDouble(valueStr);
                                            break;
                                        case "boolean":
                                            queryValue = Boolean.parseBoolean(valueStr);
                                            break;
                                        default:
                                    }
                                } catch (Exception e) {
                                    log.error("高级查询值转换失败：", e);
                                }
                            }
                            // update-begin--author:sunjianlei date:20210702 for：【/issues/I3VR8E】高级查询没有类型转换，查询参数都是字符串类型 ----
                            addEasyQuery(andWrapper, fieldColumnMap.get(rule.getField()), QueryRuleEnum.getByValue(rule.getRule()), queryValue);
                            //update-end-author:taoyan date:20201228 for: 【高级查询】 oracle 日期等于查询报错

                            // 如果拼接方式是OR，就拼接OR
                            if (MatchTypeEnum.OR == matchType && i < (filterConditions.size() - 1)) {
                                andWrapper.or();
                            }
                        }
                    }
                    //return andWrapper;
                });
            } catch (UnsupportedEncodingException e) {
                log.error("--高级查询参数转码失败：" + superQueryParams, e);
            } catch (Exception e) {
                log.error("--高级查询拼接失败：" + e.getMessage());
                e.printStackTrace();
            }
            // update-end--Author:sunjianlei  Date:20200325 for：高级查询的条件要用括号括起来，防止和用户的其他条件冲突 -------
        }
        //log.info(" superQuery getCustomSqlSegment: "+ queryWrapper.getCustomSqlSegment());
    }

    private static void doIntervalQuery(QueryWrapper<?> queryWrapper, Map<String, String[]> parameterMap, String type, String filedName, String columnName) throws ParseException {
        String endValue = null;
        String beginValue = null;
        if (parameterMap != null && parameterMap.containsKey(filedName + "_begin")) {
            beginValue = ((String[])parameterMap.get(filedName + "_begin"))[0].trim();
            addQueryByRule(queryWrapper, columnName, type, beginValue, QueryRuleEnum.GE);
        }

        if (parameterMap != null && parameterMap.containsKey(filedName + "_end")) {
            endValue = ((String[])parameterMap.get(filedName + "_end"))[0].trim();
            addQueryByRule(queryWrapper, columnName, type, endValue, QueryRuleEnum.LE);
        }

        if (parameterMap != null && parameterMap.containsKey(filedName + "_MultiString")) {
            endValue = ((String[])parameterMap.get(filedName + "_MultiString"))[0].trim();
            addQueryByRule(queryWrapper, columnName.replace("_MultiString", ""), type, endValue, QueryRuleEnum.IN);
        }

    }

    private static void addQueryByRule(QueryWrapper<?> queryWrapper, String name, String type, String value, QueryRuleEnum rule) throws ParseException {
        if (oConvertUtils.isNotEmpty(value)) {
            if (value.contains(",")) {
                Object[] temp = Arrays.stream(value.split(",")).map((v) -> {
                    try {
                        return parseByType(v, type, rule);
                    } catch (ParseException var4) {
                        ParseException e = var4;
                        e.printStackTrace();
                        return v;
                    }
                }).toArray();
                addEasyQuery(queryWrapper, name, rule, temp);
                return;
            }

            Object temp = parseByType(value, type, rule);
            addEasyQuery(queryWrapper, name, rule, temp);
        }

    }

    private static Object parseByType(String value, String type, QueryRuleEnum rule) throws ParseException {
        Object temp;
        switch (type) {
            case "class java.lang.Integer":
                temp = Integer.parseInt(value);
                break;
            case "class java.math.BigDecimal":
                temp = new BigDecimal(value);
                break;
            case "class java.lang.Short":
                temp = Short.parseShort(value);
                break;
            case "class java.lang.Long":
                temp = Long.parseLong(value);
                break;
            case "class java.lang.Float":
                temp = Float.parseFloat(value);
                break;
            case "class java.lang.Double":
                temp = Double.parseDouble(value);
                break;
            case "class java.util.Date":
                temp = getDateQueryByRule(value, rule);
                break;
            default:
                temp = value;
        }

        return temp;
    }

    private static Date getDateQueryByRule(String value, QueryRuleEnum rule) throws ParseException {
        Date date = null;
        int length = 10;
        if (value.length() == length) {
            if (rule == QueryRuleEnum.GE) {
                date = getTime().parse(value + " 00:00:00");
            } else if (rule == QueryRuleEnum.LE) {
                date = getTime().parse(value + " 23:59:59");
            }
        }

        if (date == null) {
            date = getTime().parse(value);
        }

        return date;
    }

    private static SimpleDateFormat getTime() {
        SimpleDateFormat time = (SimpleDateFormat)LOCAL.get();
        if (time == null) {
            time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            LOCAL.set(time);
        }

        return time;
    }


    private static boolean judgedIsUselessField(String name) {
        return "class".equals(name) || "ids".equals(name) || "page".equals(name) || "rows".equals(name) || "sort".equals(name) || "order".equals(name);
    }

    private static void addRuleToQueryWrapper(SysPermissionDataRuleModel dataRule, String name, Class propertyType, QueryWrapper<?> queryWrapper) {
        QueryRuleEnum rule = QueryRuleEnum.getByValue(dataRule.getRuleConditions());
        if (rule.equals(QueryRuleEnum.IN) && !propertyType.equals(String.class)) {
            String[] values = dataRule.getRuleValue().split(",");
            Object[] objs = new Object[values.length];

            for(int i = 0; i < values.length; ++i) {
                objs[i] = NumberUtils.parseNumber(values[i], propertyType);
            }

            addEasyQuery(queryWrapper, name, rule, objs);
        } else if (propertyType.equals(String.class)) {
            addEasyQuery(queryWrapper, name, rule, converRuleValue(dataRule.getRuleValue()));
        } else if (propertyType.equals(Date.class)) {
            String dateStr = converRuleValue(dataRule.getRuleValue());
            int length = 10;
            if (dateStr.length() == length) {
                addEasyQuery(queryWrapper, name, rule, DateUtils.str2Date(dateStr, (SimpleDateFormat)DateUtils.date_sdf.get()));
            } else {
                addEasyQuery(queryWrapper, name, rule, DateUtils.str2Date(dateStr, (SimpleDateFormat)DateUtils.datetimeFormat.get()));
            }
        } else {
            addEasyQuery(queryWrapper, name, rule, NumberUtils.parseNumber(dataRule.getRuleValue(), propertyType));
        }

    }

    public static void addEasyQuery(QueryWrapper<?> queryWrapper, String name, QueryRuleEnum rule, Object value) {
        if (name != null && value != null && rule != null && !oConvertUtils.isEmpty(value)) {
            name = oConvertUtils.camelToUnderline(name);
            log.debug("---高级查询 Query规则---field:{} , rule:{} , value:{}", new Object[]{name, rule.getValue(), value});
            switch (rule) {
                case GT:
                    queryWrapper.gt(name, value);
                    break;
                case GE:
                    queryWrapper.ge(name, value);
                    break;
                case LT:
                    queryWrapper.lt(name, value);
                    break;
                case LE:
                    queryWrapper.le(name, value);
                    break;
                case EQ:
                case EQ_WITH_ADD:
                    queryWrapper.eq(name, value);
                    break;
                case NE:
                    queryWrapper.ne(name, value);
                    break;
                case IN:
                    if (value instanceof String) {
                        queryWrapper.in(name, (Object[])value.toString().split(","));
                    } else if (value instanceof String[]) {
                        queryWrapper.in(name, (Object[])((Object[])value));
                    } else if (value.getClass().isArray()) {
                        queryWrapper.in(name, (Object[])((Object[])value));
                    } else {
                        queryWrapper.in(name, new Object[]{value});
                    }
                    break;
                case LIKE:
                    queryWrapper.like(name, value);
                    break;
                case LEFT_LIKE:
                    queryWrapper.likeLeft(name, value);
                    break;
                case RIGHT_LIKE:
                    queryWrapper.likeRight(name, value);
                    break;
                default:
                    log.info("--查询规则未匹配到---");
            }

        }
    }

    public static Map<String, SysPermissionDataRuleModel> getRuleMap() {
        Map<String, SysPermissionDataRuleModel> ruleMap = new HashMap(5);
        List<SysPermissionDataRuleModel> list = null;

        try {
            list = WorkflowDataAutorUtils.loadDataSearchConditon();
        } catch (Exception var5) {
            Exception e = var5;
            log.error("根据request对象获取权限数据失败，可能是定时任务中执行的。", e);
        }

        if (list != null && list.size() > 0) {
            if (list.get(0) == null) {
                return ruleMap;
            }

            SysPermissionDataRuleModel rule;
            String column;
            for(Iterator var6 = list.iterator(); var6.hasNext(); ruleMap.put(column, rule)) {
                rule = (SysPermissionDataRuleModel)var6.next();
                column = rule.getRuleColumn();
                if (QueryRuleEnum.SQL_RULES.getValue().equals(rule.getRuleConditions())) {
                    column = "SQL_RULES_COLUMN" + rule.getId();
                }
            }
        }

        return ruleMap;
    }

    public static String getSqlRuleValue(String sqlRule) {
        try {
            Set<String> varParams = getSqlRuleParams(sqlRule);

            String var;
            String tempValue;
            for(Iterator var2 = varParams.iterator(); var2.hasNext(); sqlRule = sqlRule.replace("#{" + var + "}", tempValue)) {
                var = (String)var2.next();
                tempValue = converRuleValue(var);
            }
        } catch (Exception var5) {
            Exception e = var5;
            log.error(e.getMessage(), e);
        }

        return sqlRule;
    }

    public static Set<String> getSqlRuleParams(String sql) {
        if (oConvertUtils.isEmpty(sql)) {
            return null;
        } else {
            Set<String> varParams = new HashSet();
            String regex = "\\#\\{\\w+\\}";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(sql);

            while(m.find()) {
                String var = m.group();
                varParams.add(var.substring(var.indexOf("{") + 1, var.indexOf("}")));
            }

            return varParams;
        }
    }

    public static String converRuleValue(String ruleValue) {
//        String value = JwtUtil.getUserSystemData(ruleValue, (SysUserCacheInfo)null);
        String value = null;
        return value != null ? value : ruleValue;
    }

    public static QueryRuleEnum convert2Rule(Object value) {
        if (value == null) {
            return QueryRuleEnum.EQ;
        } else {
            String val = (value + "").toString().trim();
            if (val.length() == 0) {
                return QueryRuleEnum.EQ;
            } else {
                QueryRuleEnum rule = null;
                int length2 = 2;
                int length3 = 3;
                if (rule == null && val.length() >= length3 && " ".equals(val.substring(length2, length3))) {
                    rule = QueryRuleEnum.getByValue(val.substring(0, 2));
                }

                if (rule == null && val.length() >= length2 && " ".equals(val.substring(1, length2))) {
                    rule = QueryRuleEnum.getByValue(val.substring(0, 1));
                }

                if (rule == null && val.equals("*")) {
                    rule = QueryRuleEnum.EQ;
                }

                if (rule == null && val.contains("*")) {
                    if (val.startsWith("*") && val.endsWith("*")) {
                        rule = QueryRuleEnum.LIKE;
                    } else if (val.startsWith("*")) {
                        rule = QueryRuleEnum.LEFT_LIKE;
                    } else if (val.endsWith("*")) {
                        rule = QueryRuleEnum.RIGHT_LIKE;
                    }
                }

                if (rule == null && val.contains(",")) {
                    rule = QueryRuleEnum.IN;
                }

                if (rule == null && val.startsWith("!")) {
                    rule = QueryRuleEnum.NE;
                }

                if (rule == null && val.indexOf("++") > 0) {
                    rule = QueryRuleEnum.EQ_WITH_ADD;
                }

                if (rule == QueryRuleEnum.IN && val.indexOf("yyyy-MM-dd") >= 0 && val.indexOf("to_date") >= 0) {
                    rule = QueryRuleEnum.EQ;
                }

                return rule != null ? rule : QueryRuleEnum.EQ;
            }
        }
    }

    /**
     * 替换掉关键字字符
     *
     * @param rule
     * @param value
     * @return
     */
    private static Object replaceValue(QueryRuleEnum rule, Object value) {
        if (rule == null) {
            return null;
        }
        if (! (value instanceof String)){
            return value;
        }
        String val = (value + "").toString().trim();
        //update-begin-author:taoyan date:20220302 for: 查询条件的值为等号（=）bug #3443
        if(QueryRuleEnum.EQ.getValue().equals(val)){
            return val;
        }
        //update-end-author:taoyan date:20220302 for: 查询条件的值为等号（=）bug #3443
        if (rule == QueryRuleEnum.LIKE) {
            value = val.substring(1, val.length() - 1);
            //mysql 模糊查询之特殊字符下划线 （_、\）
            value = specialStrConvert(value.toString());
        } else if (rule == QueryRuleEnum.LEFT_LIKE || rule == QueryRuleEnum.NE) {
            value = val.substring(1);
            //mysql 模糊查询之特殊字符下划线 （_、\）
            value = specialStrConvert(value.toString());
        } else if (rule == QueryRuleEnum.RIGHT_LIKE) {
            value = val.substring(0, val.length() - 1);
            //mysql 模糊查询之特殊字符下划线 （_、\）
            value = specialStrConvert(value.toString());
        } else if (rule == QueryRuleEnum.IN) {
            value = val.split(",");
        } else if (rule == QueryRuleEnum.EQ_WITH_ADD) {
            value = val.replaceAll("\\+\\+", COMMA);
        }else {
            //update-begin--Author:scott  Date:20190724 for：initQueryWrapper组装sql查询条件错误 #284-------------------
            if(val.startsWith(rule.getValue())){
                //TODO 此处逻辑应该注释掉-> 如果查询内容中带有查询匹配规则符号，就会被截取的（比如：>=您好）
                value = val.replaceFirst(rule.getValue(),"");
            }else if(val.startsWith(rule.getCondition()+QUERY_SEPARATE_KEYWORD)){
                value = val.replaceFirst(rule.getCondition()+QUERY_SEPARATE_KEYWORD,"").trim();
            }
            //update-end--Author:scott  Date:20190724 for：initQueryWrapper组装sql查询条件错误 #284-------------------
        }
        return value;
    }



    private static String specialStrConvert(String value) {
        if ("MYSQL".equals(getDbType()) || "MARIADB".equals(getDbType())) {
            String[] specialStr = "_,%".split(",");
            String[] var2 = specialStr;
            int var3 = specialStr.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String str = var2[var4];
                if (value.indexOf(str) != -1) {
                    value = value.replace(str, "\\" + str);
                }
            }
        }

        return value;
    }

    private static String getDbType() {
        return CommonUtils.getDatabaseType();
    }

    private static void doMultiFieldsOrder(QueryWrapper<?> queryWrapper,Map<String, String[]> parameterMap, Map<String,String> fieldColumnMap) {
        Set<String> allFields = fieldColumnMap.keySet();
        String column=null,order=null;
        if(parameterMap!=null&& parameterMap.containsKey(ORDER_COLUMN)) {
            column = parameterMap.get(ORDER_COLUMN)[0];
        }
        if(parameterMap!=null&& parameterMap.containsKey(ORDER_TYPE)) {
            order = parameterMap.get(ORDER_TYPE)[0];
        }
        log.debug("排序规则>>列:" + column + ",排序方式:" + order);

        //update-begin-author:scott date:2022-11-07 for:避免用户自定义表无默认字段{创建时间}，导致排序报错
        //TODO 避免用户自定义表无默认字段创建时间，导致排序报错
        if(DataBaseConstant.CREATE_TIME.equals(column) && !fieldColumnMap.containsKey(DataBaseConstant.CREATE_TIME)){
            column = "id";
            log.warn("检测到实体里没有字段createTime，改成采用ID排序！");
        }
        //update-end-author:scott date:2022-11-07 for:避免用户自定义表无默认字段{创建时间}，导致排序报错

        if (oConvertUtils.isNotEmpty(column) && oConvertUtils.isNotEmpty(order)) {
            //字典字段，去掉字典翻译文本后缀
            if(column.endsWith(CommonConstant.DICT_TEXT_SUFFIX)) {
                column = column.substring(0, column.lastIndexOf(CommonConstant.DICT_TEXT_SUFFIX));
            }

            //update-begin-author:taoyan date:2022-5-16 for: issues/3676 获取系统用户列表时，使用SQL注入生效
            //判断column是不是当前实体的
            log.debug("当前字段有："+ allFields);
            if (!allColumnExist(column, allFields)) {
                throw new WorkflowException("请注意，将要排序的列字段不存在：" + column);
            }
            //update-end-author:taoyan date:2022-5-16 for: issues/3676 获取系统用户列表时，使用SQL注入生效

            //update-begin-author:scott date:2022-10-10 for:【jeecg-boot/issues/I5FJU6】doMultiFieldsOrder() 多字段排序方法存在问题
            //多字段排序方法没有读取 MybatisPlus 注解 @TableField 里 value 的值
            if (column.contains(",")) {
                List<String> columnList = Arrays.asList(column.split(","));
                String columnStrNew = columnList.stream().map(c -> fieldColumnMap.get(c)).collect(Collectors.joining(","));
                if (oConvertUtils.isNotEmpty(columnStrNew)) {
                    column = columnStrNew;
                }
            }else{
                column = fieldColumnMap.get(column);
            }
            //update-end-author:scott date:2022-10-10 for:【jeecg-boot/issues/I5FJU6】doMultiFieldsOrder() 多字段排序方法存在问题

            //SQL注入check
            SqlInjectionUtil.filterContent(column);

            //update-begin--Author:scott  Date:20210531 for：36 多条件排序无效问题修正-------
            // 排序规则修改
            // 将现有排序 _ 前端传递排序条件{....,column: 'column1,column2',order: 'desc'} 翻译成sql "column1,column2 desc"
            // 修改为 _ 前端传递排序条件{....,column: 'column1,column2',order: 'desc'} 翻译成sql "column1 desc,column2 desc"
            if (order.toUpperCase().indexOf(ORDER_TYPE_ASC)>=0) {
                queryWrapper.orderByAsc(SqlInjectionUtil.getSqlInjectSortFields(column.split(",")));
            } else {
                queryWrapper.orderByDesc(SqlInjectionUtil.getSqlInjectSortFields(column.split(",")));
            }
            //update-end--Author:scott  Date:20210531 for：36 多条件排序无效问题修正-------
        }
    }

    /**
     * 多字段排序 判断所传字段是否存在
     * @return
     */
    private static boolean allColumnExist(String columnStr, Set<String> allFields){
        boolean exist = true;
        if(columnStr.indexOf(COMMA)>=0){
            String[] arr = columnStr.split(COMMA);
            for(String column: arr){
                if(!allFields.contains(column)){
                    exist = false;
                    break;
                }
            }
        }else{
            exist = allFields.contains(columnStr);
        }
        return exist;
    }

}
