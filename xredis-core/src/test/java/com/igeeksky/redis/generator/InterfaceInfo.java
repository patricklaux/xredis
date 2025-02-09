package com.igeeksky.redis.generator;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class InterfaceInfo {

    private String pkg;
    private String name;
    private List<String> imports;
    private List<MethodInfo> methodInfos;

    public String getName() {
        return name;
    }

    public String getPkg() {
        return pkg;
    }

    public List<String> getImports() {
        return imports;
    }

    public List<MethodInfo> getMethods() {
        return methodInfos;
    }

    public static InterfaceInfo parse(String name, String code) {
        InterfaceInfo info = new InterfaceInfo();
        info.name = name;
        info.imports = new ArrayList<>();
        info.methodInfos = new ArrayList<>();

        int range = 0;
        boolean head = false;
        String[] lines = code.split("\n");
        StringBuilder prefix = new StringBuilder();
        for (String line : lines) {
            if (line == null || (line = line.trim()).isEmpty()) {
                continue;
            }
            if (line.startsWith("import ")) {
                info.imports.add(line);
                continue;
            }
            if (line.contains("package")) {
                info.pkg = line.replace("package", "").trim();
                continue;
            }
            if (line.startsWith("public interface ")) {
                if (!line.endsWith("{")) {
                    head = true;
                }
                continue;
            }
            if (head && !line.endsWith("{")) {
                continue;
            }
            if (head && line.endsWith("{")) {
                head = false;
                continue;
            }
            if (line.startsWith("default")) {
                if (line.contains("{")) {
                    range++;
                }
                continue;
            }
            if (line.contains("{")) {
                range++;
            }
            if (line.contains("}")) {
                range--;
            }
            if (range > 0) {
                continue;
            }
            if (line.startsWith("}")) {
                continue;
            }
            if (line.contains("@Override")) {
                continue;
            }
            if (line.contains("@Deprecated")) {
                continue;
            }
            if (!line.endsWith(";")) {
                prefix.append(line);
                continue;
            }
            if (!prefix.isEmpty()) {
                line = prefix + line;
                prefix = new StringBuilder();
            }
            info.methodInfos.add(MethodInfo.parse(line));
        }
        return info;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InterfaceInfo that)) return false;

        return Objects.equals(pkg, that.pkg) && Objects.equals(name, that.name) && Objects.equals(imports, that.imports) && Objects.equals(methodInfos, that.methodInfos);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(pkg);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(imports);
        result = 31 * result + Objects.hashCode(methodInfos);
        return result;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}