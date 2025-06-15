package com.epiroc.workflow.common.util;

/**
 * 字符串工具类
 *
 * @author Theo Zheng
 * @version 1.0
 * <p>
 * Copyright (c) 2025 Epiroc (Nanjing) Construction and Mining Equipment Ltd. All rights reserved.
 * @date 2025-06-14
 */
public class StringUtil {

    public static String joinWithChar(String str1, String str2, String connector) {
        // 处理null字符串
        String s1 = (str1 == null) ? "" : str1;
        String s2 = (str2 == null) ? "" : str2;

        // 使用StringBuilder高效拼接
        StringBuilder result = new StringBuilder(s1);
        if (!s1.isEmpty() && !s2.isEmpty()) {
            result.append(connector);
        }
        result.append(s2);

        return result.toString();
    }

}
