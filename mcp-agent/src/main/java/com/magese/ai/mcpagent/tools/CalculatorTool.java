package com.magese.ai.mcpagent.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 计算器工具
 *
 * @author Magese
 * @since 2025/9/1 15:39
 */
@Slf4j
@Component
public class CalculatorTool {

    @Tool("计算两个数相加的和")
    public double add(double a, double b) {
        log.info("add: {} + {}", a, b);
        return a + b;
    }

    @Tool("计算指定数字的平方根")
    public double squareRoot(double x) {
        log.info("squareRoot: {}", x);
        return Math.sqrt(x);
    }
}
