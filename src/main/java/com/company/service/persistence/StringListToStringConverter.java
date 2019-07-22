package com.company.service.persistence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class StringListToStringConverter implements AttributeConverter<List<String>,String> {
    public final static Character SEPARATOR_CHAR = '|';

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) return null;

        StringBuilder result = new StringBuilder("");
        Iterator<String> iter = attribute.iterator();
        while (iter.hasNext()) {
            String value = iter.next();
            for (Character ch : value.toCharArray()) {
                if (ch.equals(SEPARATOR_CHAR)) {
                    result.append("||");
                } else {
                    result.append(ch);
                }
            }

            if (iter.hasNext()) {
                result.append(SEPARATOR_CHAR);
            }
        }

        return result.toString();
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;

        ArrayList<String> result = new ArrayList<>();

        // immediately return on an empty string
        if (dbData == "") return result;

        StringBuilder word = new StringBuilder();
        for (int i = 0; i < dbData.length(); i++) {
            Character ch = dbData.charAt(i);

            if (ch.equals(SEPARATOR_CHAR)) {
                // if i has not yet reached the end of the input, look at the next character
                // if it is an escaped separator
                if (i < dbData.length() - 1 && dbData.charAt(i + 1) == SEPARATOR_CHAR) {
                    word.append(SEPARATOR_CHAR);
                    // skip the additional separator
                    i++;
                } else {
                    result.add(word.toString());
                    word = new StringBuilder();
                }
            } else {
                word.append(ch);

                if (i == dbData.length() - 1) {
                    // end of string 'dbData' reached
                    result.add(word.toString());
                }
            }
        }

        return result;
    }
}