package com.igeeksky.xredis.lettuce.generator;

import com.igeeksky.xtool.core.KeyValue;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.tuple.Tuple2;
import com.igeeksky.xtool.core.tuple.Tuples;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 自动代码生成器
 * <p>
 * 用于生成 LettuceSyncOperator 及 LettuceClusterSyncOperator
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
@Disabled
public class LettuceSyncGeneratorTest {

    private static final String GROUP_ID = "io.lettuce";
    private static final String VERSION = "6.5.2.RELEASE";
    private static final String ARTIFACT_ID = "lettuce-core";
    private static final String BASE_URL = "https://maven.aliyun.com/repository/public/";
    private static final String BASE_PATH = "src/test/resources/";
    private static final String SOURCE_PATH = "io/lettuce/core/api/sync/";
    private static final String SOURCE_PATH2 = "io/lettuce/core/cluster/api/sync/";

    // 注释正则表达式
    private static final String COMMENT_REG = "/\\*.*?\\*/";

    @Test
    @Disabled
    void generateSync() {
        String[] files = new String[]{
                "BaseRedisCommands.java",
                "RedisAclCommands.java",
                "RedisFunctionCommands.java",
                "RedisGeoCommands.java",
                "RedisHashCommands.java",
                "RedisHLLCommands.java",
                "RedisJsonCommands.java",
                "RedisKeyCommands.java",
                "RedisListCommands.java",
                "RedisScriptingCommands.java",
                "RedisServerCommands.java",
                "RedisSetCommands.java",
                "RedisSortedSetCommands.java",
                "RedisStreamCommands.java",
                "RedisStringCommands.java",
                "RedisCommands.java",
                "RedisTransactionalCommands.java",
                "RedisClusterCommands.java",
        };

        String head = """
                import com.igeeksky.redis.api.RedisSyncOperator;
                import io.lettuce.core.api.sync.RedisCommands;
                import io.lettuce.core.api.StatefulConnection;
                
                /**
                 * Lettuce 同步方法
                 *
                 * @author Patrick.Lau
                 * @since 1.0.0
                 */
                public class LettuceSyncOperator<K, V> implements RedisSyncOperator<K, V>, RedisCommands<K, V> {
                
                    private final RedisCommands<K, V> redisCommands;
                    private final StatefulRedisConnection<K, V> connection;
                
                    public LettuceSyncOperator(StatefulRedisConnection<K, V> connection) {
                        this.connection = connection;
                        this.redisCommands = connection.sync();
                    }
                """;

        String foot = """
                    public StatefulConnection<K, V> getConnection() {
                        return connection;
                    }
                
                }
                """;

        String generated = generate(files, "com.igeeksky.redis.lettuce;", head, foot);
        System.out.println(generated);
        System.out.println(generated.length());

        // Files.write(Path.of(BASE_PATH + "LettuceSyncOperator.java"), generated.getBytes());
    }

    @Test
    @Disabled
    void generateClusterSync() {
        String[] files = new String[]{
                "BaseRedisCommands.java",
                "RedisAclCommands.java",
                "RedisFunctionCommands.java",
                "RedisGeoCommands.java",
                "RedisHashCommands.java",
                "RedisHLLCommands.java",
                "RedisJsonCommands.java",
                "RedisKeyCommands.java",
                "RedisListCommands.java",
                "RedisScriptingCommands.java",
                "RedisServerCommands.java",
                "RedisSetCommands.java",
                "RedisSortedSetCommands.java",
                "RedisStreamCommands.java",
                "RedisStringCommands.java",
                "RedisClusterCommands.java",
                "RedisAdvancedClusterCommands.java"
        };

        String head = """
                import com.igeeksky.redis.api.RedisSyncOperator;
                import io.lettuce.core.cluster.api.sync.NodeSelection;
                import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
                import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
                import io.lettuce.core.api.StatefulConnection;
                
                /**
                 * Lettuce 集群同步方法
                 *
                 * @author Patrick.Lau
                 * @since 1.0.0
                 */
                public class LettuceClusterSyncOperator<K, V> implements RedisSyncOperator<K, V>, RedisAdvancedClusterCommands<K, V> {
                
                    private final RedisAdvancedClusterCommands<K, V> redisCommands;
                    private final StatefulRedisClusterConnection<K, V> connection;
                
                    public LettuceClusterSyncOperator(StatefulRedisClusterConnection<K, V> connection) {
                        this.connection = connection;
                        this.redisCommands = connection.sync();
                    }
                """;

        String foot = """
                    public StatefulConnection<K, V> getConnection() {
                         return connection;
                    }
                
                }
                """;

        String generated = generate(files, "com.igeeksky.redis.cluster;", head, foot);
        System.out.println(generated);
        System.out.println(generated.length());

        // Files.write(Path.of(BASE_PATH + "LettuceClusterSyncOperator.java"), generated.getBytes());
    }

    public static String generate(String[] files, String pkg, String head, String foot) {
        // 下载源码包
        downloadSources(GROUP_ID, ARTIFACT_ID, VERSION);

        // 解压源码包
        decompress(createFilepath(ARTIFACT_ID, VERSION), BASE_PATH);

        // 解析接口列表
        Tuple2<Set<String>, Set<MethodInfo>> tuple2 = parseInterfaces(files);

        // 开始生成代码
        StringBuilder builder = new StringBuilder(200000);

        // 添加包名信息
        builder.append("package ").append(pkg).append("\n").append("\n");

        // 添加导入包信息
        for (String imp : tuple2.getT1()) {
            builder.append(imp).append("\n");
        }

        // 添加类头信息
        builder.append(head);

        // 生成方法代码
        generateMethodsCode(tuple2.getT2(), builder);

        // 添加类尾信息
        builder.append(foot).append("\n\n");

        return builder.toString();
    }

    /**
     * 生成方法代码
     *
     * @param methodInfos 所有方法信息
     * @param builder     代码字符容器
     */
    private static void generateMethodsCode(Set<MethodInfo> methodInfos, StringBuilder builder) {
        if (CollectionUtils.isEmpty(methodInfos)) {
            return;
        }
        for (MethodInfo methodInfo : methodInfos) {
            String name = methodInfo.getName();
            List<KeyValue<String, String>> params = methodInfo.getParams();
            boolean hasVarargs = isVarargs(params);
            builder.append("    @Override").append("\n");
            if (hasVarargs) {
                builder.append("@SafeVarargs\n");
            }
            builder.append("    public ");
            if (hasVarargs) {
                builder.append("final ");
            }
            builder.append(methodInfo.getOriginal())
                    .append("{")
                    .append("\n");

            String returnType = methodInfo.getReturnType();
            if (returnType.contains("void")) {
                builder.append("        redisCommands.").append(name).append("(");
            } else {
                builder.append("        return redisCommands.").append(name).append("(");
            }
            // 处理方法参数
            int size = params.size();
            for (int i = 0; i < size; i++) {
                builder.append(params.get(i).getValue());
                if (i < size - 1) {
                    builder.append(", ");
                }
            }
            builder.append(");");
            builder.append("\n").append("    }").append("\n\n");
        }
    }

    /**
     * 判断是否为带泛型的可变参数
     *
     * @param params 参数列表
     * @return 是否可变参数
     */
    private static boolean isVarargs(List<KeyValue<String, String>> params) {
        if (CollectionUtils.isNotEmpty(params)) {
            KeyValue<String, String> param = params.getLast();
            String key = param.getKey();
            if (key.contains("...")) {
                if (key.contains("V...") || key.contains("K...")) {
                    return true;
                }
                return key.contains("<") && key.contains(">");
            }
        }
        return false;
    }

    /**
     * 解析接口
     *
     * @param files 接口文件列表
     * @return t1. 所有导入包；t2. 所有方法信息
     */
    public static Tuple2<Set<String>, Set<MethodInfo>> parseInterfaces(String[] files) {
        Set<String> allImports = new LinkedHashSet<>();
        Set<MethodInfo> allMethodInfos = new LinkedHashSet<>();
        for (String file : files) {
            String filename = BASE_PATH + SOURCE_PATH + file;
            if (!Files.exists(Path.of(filename))) {
                filename = BASE_PATH + SOURCE_PATH2 + file;
            }
            if (!Files.exists(Path.of(filename))) {
                throw new RuntimeException(filename + " not exists");
            }
            String code = removeComment(filename);
            InterfaceInfo info = InterfaceInfo.parse(filename, code);
            allImports.addAll(info.getImports());
            allMethodInfos.addAll(info.getMethods());
        }
        return Tuples.of(allImports, allMethodInfos);
    }

    public static String removeComment(String src) {
        StringBuilder builder = new StringBuilder(500000);

        try (BufferedReader br = new BufferedReader(new FileReader(src))) {
            String line;
            while ((line = br.readLine()) != null) {
                if ((line = line.trim()).isEmpty()) {
                    continue;
                }
                if (line.startsWith("//")) {
                    continue;
                }
                builder.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Pattern pattern = Pattern.compile(COMMENT_REG, Pattern.DOTALL);
        return pattern.matcher(builder.toString()).replaceAll("");
    }

    public static void decompress(String zipFilePath, String destDirectory) {
        try (ZipInputStream stream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = stream.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                String filePath = destDirectory + entry.getName();
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    extractFile(stream, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    if (!dir.exists()) {
                        boolean mkdir = dir.mkdir();
                        if (!mkdir) {
                            System.out.println("创建目录失败：" + filePath);
                        }
                    }
                }
                stream.closeEntry();
                entry = stream.getNextEntry();
            }
        } catch (IOException e) {
            System.out.println("解压文件失败：" + zipFilePath);
            throw new RuntimeException(e);
        }
    }

    public static void extractFile(ZipInputStream stream, String filePath) {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = stream.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        } catch (IOException e) {
            System.out.println("解压文件失败：" + filePath);
            throw new RuntimeException(e);
        }
    }

    public static void downloadSources(String groupId, String artifactId, String version) {
        String filepath = createFilepath(artifactId, version);

        if (Files.exists(Path.of(filepath))) {
            return;
        }

        try {
            // 构建源码包的 URL
            String path = groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + "-sources.jar";
            String url = BASE_URL + path;

            // 创建 URL 对象
            URL sourceUrl = URI.create(url).toURL();
            HttpURLConnection connection = (HttpURLConnection) sourceUrl.openConnection();
            connection.setRequestMethod("GET");

            int code = connection.getResponseCode();
            // 检查响应码
            if (HttpURLConnection.HTTP_OK == code) {
                // 读取输入流
                InputStream inputStream = connection.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(filepath);
                // 写入文件
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // 关闭流
                outputStream.close();
                inputStream.close();
            } else {
                System.out.println("Failed to download sources. Response code: " + code);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String createFilepath(String artifactId, String version) {
        return BASE_PATH + artifactId + "-" + version + "-sources.jar";
    }

}
