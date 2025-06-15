package com.epiroc.workflow.common.system.handler;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JsonListTypeHandler extends AbstractJsonTypeHandler<List<Integer>> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected List<Integer> parse(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return Collections.emptyList();
            }
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<Integer>>() {});
        } catch (IOException e) {
            throw new RuntimeException("JSON 解析失败: " + json, e);
        }
    }

    @Override
    protected String toJson(List<Integer> obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 序列化失败: " + obj, e);
        }
    }
}
