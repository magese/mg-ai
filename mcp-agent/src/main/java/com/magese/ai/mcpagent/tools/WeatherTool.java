package com.magese.ai.mcpagent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 天气工具
 *
 * @author Magese
 * @since 2025/9/1 16:01
 */
@Slf4j
@Component
public class WeatherTool {

    @Tool("获取指定时间和地点的天气")
    public String getWeather(@P("需要获取天气的日期") String date, @P("需要获取天气的地点") String location) {
        log.info("getWeather: date={}, location={}", date, location);
        return "2025年9月1日，广州天河区天气晴，气温26~33度。";
    }
}
