package com.igeeksky.xredis.lettuce.generator;

import com.igeeksky.xtool.core.KeyValue;
import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MethodInfo {

    private String name;
    private String original;
    private String returnType;
    private List<KeyValue<String, String>> params;

    public String getName() {
        return name;
    }

    public String getOriginal() {
        return original;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<KeyValue<String, String>> getParams() {
        return params;
    }

    public static MethodInfo parse(String line) {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.original = line.substring(0, line.length() - 1);
        int begin = line.indexOf("(");
        int end = line.indexOf(")");
        String params = line.substring(begin + 1, end);

        String prefix = line.substring(0, begin);
        int index1 = prefix.lastIndexOf(" ");
        methodInfo.returnType = prefix.substring(0, index1);
        methodInfo.name = prefix.substring(index1 + 1);
        methodInfo.params = parseParams(params);
        return methodInfo;
    }

    public static List<KeyValue<String, String>> parseParams(String params) {
        List<KeyValue<String, String>> keyValues = new ArrayList<>();
        params = params.trim();
        if (params.isEmpty()) {
            return keyValues;
        }

        int l = 0, begin = 0, blank = 0, length = params.length();
        for (int i = 0; i < length; i++) {
            char c = params.charAt(i);
            if (c == '<') {
                ++l;
            }
            if (c == '>') {
                --l;
            }
            if (c == ' ') {
                blank = i;
            }
            if (c == ',') {
                if (l == 0) {
                    String key = params.substring(begin, blank).trim();
                    String val = params.substring(blank + 1, i).trim();
                    keyValues.add(KeyValue.create(key, val));
                    begin = i + 1;
                }
            }
        }

        if (l == 0) {
            String key = params.substring(begin, blank).trim();
            String val = params.substring(blank + 1, length).trim();
            keyValues.add(KeyValue.create(key, val));
        } else {
            throw new IllegalArgumentException("[" + params + "]params error");
        }

        return keyValues;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodInfo methodInfo)) return false;

        return Objects.equals(name, methodInfo.name) && Objects.equals(original, methodInfo.original) && Objects.equals(returnType, methodInfo.returnType) && Objects.equals(params, methodInfo.params);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(original);
        result = 31 * result + Objects.hashCode(returnType);
        result = 31 * result + Objects.hashCode(params);
        return result;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}