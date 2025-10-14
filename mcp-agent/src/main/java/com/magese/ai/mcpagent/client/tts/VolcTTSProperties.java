package com.magese.ai.mcpagent.client.tts;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 火山云语音合成配置项
 *
 * @author Magese
 * @since 2025/10/11 11:56
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "volc.tts")
public class VolcTTSProperties {
    /**
     * appId
     */
    private String appId;
    /**
     * 访问密钥
     */
    private String accessToken;
    /**
     * 请求端点
     */
    private String endpoint = "wss://openspeech.bytedance.com/api/v3/tts/bidirection";
    /**
     * 调用服务的资源信息 ID
     * 豆包语音合成模型1.0：
     * seed-tts-1.0
     * seed-tts-1.0-concurr
     * 豆包语音合成模型2.0:
     * seed-tts-2.0
     * 声音复刻2.0（不支持声音复刻1.0）：
     * volc.megatts.default
     * volc.megatts.concurr
     */
    private String voice;
    /**
     * 音频编码格式，mp3/ogg_opus/pcm。
     * 接口传入wav并不会报错，在流式场景下传入wav会多次返回wav header，这种场景建议使用pcm。
     */
    private String encoding;
}
