package com.magese.ai.mcpserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * 计算器服务
 *
 * @author Magese
 * @since 2025/8/27 09:36
 */
@Slf4j
@Service
public class CalculatorService {

    /**
     * Add two numbers
     *
     * @param a The first number
     * @param b The second number
     * @return The sum of the two numbers
     */
    @Tool(description = "两数相加")
    public String add(double a, double b) {
        log.info("两数相加：{} + {}", a, b);
        double result = a + b;
        return formatResult(a, "+", b, result);
    }

    /**
     * Subtract one number from another
     *
     * @param a The number to subtract from
     * @param b The number to subtract
     * @return The result of the subtraction
     */
    @Tool(description = "两数相减")
    public String subtract(double a, double b) {
        log.info("两数相加：{} - {}", a, b);
        double result = a - b;
        return formatResult(a, "-", b, result);
    }

    /**
     * Multiply two numbers
     *
     * @param a The first number
     * @param b The second number
     * @return The product of the two numbers
     */
    @Tool(description = "两数相乘")
    public String multiply(double a, double b) {
        log.info("两数相乘：{} * {}", a, b);
        double result = a * b;
        return formatResult(a, "*", b, result);
    }

    /**
     * Divide one number by another
     *
     * @param a The numerator
     * @param b The denominator
     * @return The result of the division
     */
    @Tool(description = "两数相除")
    public String divide(double a, double b) {
        log.info("两数相除：{} / {}", a, b);
        if (b == 0) {
            return "Error: Cannot divide by zero";
        }
        double result = a / b;
        return formatResult(a, "/", b, result);
    }

    /**
     * Calculate the power of a number
     *
     * @param base     The base number
     * @param exponent The exponent
     * @return The result of raising the base to the exponent
     */
    @Tool(description = "两数次幂")
    public String power(double base, double exponent) {
        log.info("两数次幂：{} ^ {}", base, exponent);
        double result = Math.pow(base, exponent);
        return formatResult(base, "^", exponent, result);
    }

    /**
     * Calculate the square root of a number
     *
     * @param number The number to find the square root of
     * @return The square root of the number
     */
    @Tool(description = "开平方根")
    public String squareRoot(double number) {
        log.info("开平方根：{}", number);
        if (number < 0) {
            return "Error: Cannot calculate square root of a negative number";
        }
        double result = Math.sqrt(number);
        return String.format("√%.2f = %.2f", number, result);
    }

    /**
     * Calculate the modulus (remainder) of division
     *
     * @param a The dividend
     * @param b The divisor
     * @return The remainder of the division
     */
    @Tool(description = "两数取模")
    public String modulus(double a, double b) {
        log.info("两数取模：{} % {}", a, b);
        if (b == 0) {
            return "Error: Cannot divide by zero";
        }
        double result = a % b;
        return formatResult(a, "%", b, result);
    }

    /**
     * Calculate the absolute value of a number
     *
     * @param number The number to find the absolute value of
     * @return The absolute value of the number
     */
    @Tool(description = "取绝对值")
    public String absolute(double number) {
        log.info("取绝对值：{}", number);
        double result = Math.abs(number);
        return String.format("|%.2f| = %.2f", number, result);
    }

    /**
     * Get help about available calculator operations
     *
     * @return Information about available operations
     */
    @Tool(description = "获取计算器工具的操作帮助")
    public String help() {
        return """
                Basic Calculator MCP Service
                
                可用的操作:
                1. add(a, b) - 两数相加
                2. subtract(a, b) - 两数相减
                3. multiply(a, b) - 两数相乘
                4. divide(a, b) - 两数相除
                5. power(base, exponent) - 两数次幂
                6. squareRoot(number) - 开平方根
                7. modulus(a, b) - 两数取模
                8. absolute(number) - 取绝对值
                
                使用示例：add(5, 3) will return 5 + 3 = 8
                """;
    }

    /**
     * Format the result of a calculation
     */
    private String formatResult(double a, String operator, double b, double result) {
        return String.format("%.2f %s %.2f = %.2f", a, operator, b, result);
    }
}
