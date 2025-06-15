package com.epiroc.workflow.common.util;

import com.epiroc.workflow.common.system.constant.CommonConstant;

public class IdUtil {

    public static String resetGuidStr(String guid) {
        int len = 0;
        if(guid.length() < CommonConstant.BASE_GUID_LENGTH){
            len = CommonConstant.BASE_GUID_LENGTH - guid.length();
        }
        for(int i = 0;i < len;i++){
            guid = CommonConstant.STRING_ZERO + guid;
        }
        return guid;
    }

}
