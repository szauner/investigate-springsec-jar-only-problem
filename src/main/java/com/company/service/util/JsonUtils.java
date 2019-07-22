package com.company.service.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    public static Map<String, Object> convertJsonToMap(String jsonString)
    throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        return mapper.readValue(jsonString, typeRef);
    }

    public static String convertMapToJson(Map<String, String> data)
    throws JsonParseException, JsonMappingException, IOException {
        return new ObjectMapper().writeValueAsString(data);
    }
}