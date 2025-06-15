package com.epiroc.workflow.common.util;

import com.epiroc.workflow.common.system.constant.CommonConstant;

/**
 * @author : Theo Zheng
 * @version : V1.0
 * @project : antbi
 * @file : OrderNoUtil
 * @description : 订单编号工具类
 * @date : 2021-09-26 13:06
 * @Copyright : 2020 Epiroc Trading Co., Ltd. All rights reserved.
 */
public class OrderNoUtil implements CommonConstant {

    /**
     * 生成 oederNo
     * @param orderNoPre
     * @return
     */
    public static String generateOrderNo(String orderNoPre, Integer length){
        String orderNo = null;

        orderNo = orderNoPre + UNIT_SHORT_LINE + UUIDGenerator.getUUIDBits(length);

        return orderNo;
    }

}
