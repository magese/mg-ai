package com.magese.ai.mcpagent.domain;

import com.magese.ai.mcpagent.util.JacksonUtil;

/**
 * 转换为Json字符串接口
 *
 * @author Magese
 * @since 2025/10/14 15:05
 */
public interface ToJsonString {
    default  String toJsonString() {
        return JacksonUtil.toJsonString(this);
    }
}
