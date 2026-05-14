package com.magese.ai.agent.youcash;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;

/**
 * youcash代理
 *
 * @author Magese
 * @since 2026/5/13 17:21
 */
@Slf4j
@SpringBootApplication
public class YoucashAgentApp {

    public static void main(String[] args) {
        try {
            log.info("Youcash Agent 开始启动...");
            SpringApplication.run(YoucashAgentApp.class, args);
            ServerProperties serverProperties = SpringUtil.getBean(ServerProperties.class);
            log.info("Youcash Agent 启动成功 [http://127.0.0.1:{}]", serverProperties.getPort());
        } catch (Exception e) {
            log.error("Youcash Agent 启动异常", e);
        }
    }
}
