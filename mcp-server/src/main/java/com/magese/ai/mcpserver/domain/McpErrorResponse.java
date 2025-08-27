package com.magese.ai.mcpserver.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP异常响应
 *
 * @author Magese
 * @since 2025/8/27 10:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpErrorResponse implements java.io.Serializable {
    private String code;
    private String message;
}
