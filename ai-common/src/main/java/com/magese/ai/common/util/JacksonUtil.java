package com.magese.ai.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantLock;

/**
 * json工具类
 *
 * @author Magese
 * @since 2025/10/11 17:16
 */
@SuppressWarnings("unused")
@Slf4j
public class JacksonUtil {

    /**
     * -- GETTER --
     * 获取底层的 ObjectMapper 实例
     * 用于需要更复杂操作的场景
     */
    @Getter
    private static ObjectMapper objectMapper;

    private static final ReentrantLock lock = new ReentrantLock();

    static {
        initializeDefaultObjectMapper();
    }

    /**
     * 初始化默认的 ObjectMapper
     */
    private static void initializeDefaultObjectMapper() {
        lock.lock();
        try {
            objectMapper = JsonMapper.builder()
                    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS,
                            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                            SerializationFeature.INDENT_OUTPUT)
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                    .build();

            // 设置日期格式和时区
            objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));

            // 注册 Java 8 时间模块
            objectMapper.registerModule(new JavaTimeModule());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 对象转换为 JSON 字符串
     *
     * @param object 要转换的对象
     * @return JSON 字符串
     */
    public static String toJsonString(Object object) {
        if (object == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("对象转JSON失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON转换失败", e);
        }
    }

    /**
     * 对象转换为 JSON 字节数组
     *
     * @param object 要转换的对象
     * @return JSON 字节数组
     */
    public static byte[] toJsonBytes(Object object) {
        if (object == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            log.error("对象转JSON字节数组失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON字节数组转换失败", e);
        }
    }

    /**
     * 对象转换为格式化的 JSON 字符串
     *
     * @param object 要转换的对象
     * @return 格式化的 JSON 字符串
     */
    public static String toPrettyJsonString(Object object) {
        if (object == null) {
            return null;
        }

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("对象转格式化JSON失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON转换失败", e);
        }
    }

    /**
     * JSON 字符串转换为对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标对象类型
     * @param <T>   对象泛型
     * @return 转换后的对象
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON转对象失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON解析失败", e);
        }
    }

    /**
     * JSON 字节数组转换为对象
     *
     * @param jsonBytes JSON 字节数组
     * @param clazz     目标对象类型
     * @param <T>       对象泛型
     * @return 转换后的对象
     */
    public static <T> T parseObject(byte[] jsonBytes, Class<T> clazz) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }

        try {
            return objectMapper.readValue(jsonBytes, clazz);
        } catch (IOException e) {
            log.error("JSON字节数组转对象失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON字节数组解析失败", e);
        }
    }

    /**
     * JSON 字符串转换为复杂类型对象
     *
     * @param json          JSON 字符串
     * @param typeReference 类型引用，用于复杂类型转换
     * @param <T>           对象泛型
     * @return 转换后的对象
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("JSON转复杂对象失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON解析失败", e);
        }
    }

    /**
     * JSON 字节数组转换为复杂类型对象
     *
     * @param jsonBytes     JSON 字节数组
     * @param typeReference 类型引用，用于复杂类型转换
     * @param <T>           对象泛型
     * @return 转换后的对象
     */
    public static <T> T parseObject(byte[] jsonBytes, TypeReference<T> typeReference) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }

        try {
            return objectMapper.readValue(jsonBytes, typeReference);
        } catch (IOException e) {
            log.error("JSON字节数组转复杂对象失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON字节数组解析失败", e);
        }
    }

    /**
     * JSON 字符串转换为 List
     *
     * @param json  JSON 字符串
     * @param clazz List 元素类型
     * @param <T>   元素泛型
     * @return 转换后的 List
     */
    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            log.error("JSON转List失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON解析失败", e);
        }
    }

    /**
     * JSON 字节数组转换为 List
     *
     * @param jsonBytes JSON 字节数组
     * @param clazz     List 元素类型
     * @param <T>       元素泛型
     * @return 转换后的 List
     */
    public static <T> List<T> parseArray(byte[] jsonBytes, Class<T> clazz) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }

        try {
            JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(jsonBytes, javaType);
        } catch (IOException e) {
            log.error("JSON字节数组转List失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON字节数组解析失败", e);
        }
    }

    /**
     * JSON 字符串转换为 Map
     *
     * @param json JSON 字符串
     * @return 转换后的 Map
     */
    public static Map<String, Object> parseMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON转Map失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON解析失败", e);
        }
    }

    /**
     * JSON 字节数组转换为 Map
     *
     * @param jsonBytes JSON 字节数组
     * @return 转换后的 Map
     */
    public static Map<String, Object> parseMap(byte[] jsonBytes) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }

        try {
            return objectMapper.readValue(jsonBytes, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("JSON字节数组转Map失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON字节数组解析失败", e);
        }
    }

    /**
     * 对象转换（深拷贝）
     *
     * @param source 源对象
     * @param clazz  目标类型
     * @param <T>    对象泛型
     * @return 转换后的新对象
     */
    public static <T> T convertValue(Object source, Class<T> clazz) {
        if (source == null) {
            return null;
        }

        return objectMapper.convertValue(source, clazz);
    }

    /**
     * 判断字符串是否为有效 JSON
     *
     * @param json 待验证的字符串
     * @return 是否为有效 JSON
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }

        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 判断字节数组是否为有效 JSON
     *
     * @param jsonBytes 待验证的字节数组
     * @return 是否为有效 JSON
     */
    public static boolean isValidJson(byte[] jsonBytes) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return false;
        }

        try {
            objectMapper.readTree(jsonBytes);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
