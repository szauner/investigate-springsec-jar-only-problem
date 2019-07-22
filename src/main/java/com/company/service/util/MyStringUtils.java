package com.company.service.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class MyStringUtils {
    public static List<String> convertCsvToList(String source) {
        ArrayList<String> list = new ArrayList<String>();

        for(String singleValue : source.split(",")) {
            if (StringUtils.isNotBlank(singleValue)) {
                list.add(singleValue.trim());
            }
        }

        return list;
    }
}
