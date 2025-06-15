package com.epiroc.workflow.common.service;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * @author : Theo Zheng
 * @version : V1.0
 * @project : antbi
 * @file : WfMailSendService
 * @description : TODO
 * @date : 2021-10-22 13:39
 * @Copyright : 2021 Epiroc Trading Co., Ltd. All rights reserved.
 */
public interface WfMailSendService {

    JSONObject sendSubmitApproveMail(Map<String, String> messageMap, String nextOperatorEmail, String status);

}
