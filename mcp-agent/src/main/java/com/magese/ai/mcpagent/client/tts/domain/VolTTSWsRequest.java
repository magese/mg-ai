package com.magese.ai.mcpagent.client.tts.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magese.ai.mcpagent.util.JacksonUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * 火山云语音合成请求
 *
 * @param user      用户信息
 * @param event     请求的事件类型
 * @param namespace 请求方法命名空间，默认值："BidirectionalTTS"
 * @param reqParams 请求参数
 * @author Magese
 * @since 2025/10/11 14:51
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VolTTSWsRequest(
        User user,
        Integer event,
        @NotNull String namespace,
        @JsonProperty("req_params")
        @NotNull ReqParams reqParams
) {
    public VolTTSWsRequest withEvent(Integer event) {
        return new VolTTSWsRequest(this.user, event, this.namespace, this.reqParams);
    }

    public VolTTSWsRequest withReqParams(ReqParams reqParams) {
        return new VolTTSWsRequest(this.user, this.event, this.namespace, reqParams);
    }

    /**
     * 用户信息
     *
     * @param uid 用户ID
     */
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record User(
            @NotNull String uid
    ) {}

    /**
     * 请求参数
     *
     * @param text        输入文本，text和ssml至少有一个不为空
     * @param model       模型版本，传seed-tts-1.1较默认版本音质有提升，并且延时更优
     * @param ssml        当文本格式是ssml时，需要将文本赋值为ssml，此时文本处理的优先级高于text
     * @param speaker     发音人
     * @param audioParams 音频参数
     * @param additions   用户自定义参数
     * @param mixSpeaker  混音参数结构，仅适用于"豆包语音合成模型1.0"的音色
     */
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ReqParams(
            String text,
            String model,
            String ssml,
            @NotNull String speaker,
            @NotNull @JsonProperty("audio_params") AudioParams audioParams,
            String additions,
            @JsonProperty("mix_speaker") MixSpeaker mixSpeaker
    ) {
        public ReqParams withText(String text) {
            return new ReqParams(text, this.model, this.ssml, this.speaker, this.audioParams, this.additions, this.mixSpeaker);
        }

        /**
         * 音频参数配置
         *
         * @param format          音频编码格式，可选值："mp3","ogg_opus","pcm"，默认值："mp3"
         * @param sampleRate      音频采样率，可选值：[8000,16000,22050,24000,32000,44100,48000]，默认值：24000
         * @param bitRate         音频比特率，示例：16000,32000等，默认范围64k～160k
         * @param emotion         设置音色的情感，示例："angry"，仅部分音色支持
         * @param emotionScale    情绪强度调节，范围1~5，默认值：4
         * @param speechRate      语速调节，取值范围[-50,100]，默认值：0，100代表2.0倍速
         * @param loudnessRate    音量调节，取值范围[-50,100]，默认值：0，100代表2.0倍音量
         * @param enableTimestamp 是否返回字与音素时间戳，默认值：false，仅适用于"豆包语音合成模型1.0"的音色
         * @param customParams    用于存储其他可能的扩展参数
         */
        @Builder
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record AudioParams(
                String format,
                @JsonProperty("sample_rate") Integer sampleRate,
                @JsonProperty("bit_rate") Integer bitRate,
                String emotion,
                @JsonProperty("emotion_scale") Integer emotionScale,
                @JsonProperty("speech_rate") Integer speechRate,
                @JsonProperty("loudness_rate") Integer loudnessRate,
                @JsonProperty("enable_timestamp") Boolean enableTimestamp,
                Map<String, Object> customParams
        ) {}

        /**
         * 混音参数配置
         *
         * @param speakers 混音音色名以及影响因子列表，最多支持3个音色混音，混音影响因子和必须=1
         */
        @Builder
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record MixSpeaker(
                List<SpeakerMix> speakers
        ) {
            /**
             * 单个混音源配置
             *
             * @param sourceSpeaker 混音源音色名，支持大小模型音色和复刻2.0音色
             * @param mixFactor     混音源音色名影响因子，默认值：0.0，所有speaker的mixFactor之和必须等于1
             */
            @Builder
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public record SpeakerMix(
                    @NotBlank @JsonProperty("source_speaker") String sourceSpeaker,
                    @JsonProperty("mix_factor") Float mixFactor
            ) {}
        }

        /**
         * 附加参数配置
         *
         * @param silenceDuration              句尾增加静音时长，范围0~30000ms，默认值：0
         * @param enableLanguageDetector       自动识别语种，默认值：false
         * @param disableMarkdownFilter        是否开启markdown解析过滤，默认值：false
         * @param disableEmojiFilter           是否开启emoji表情过滤，默认值：false
         * @param muteCutRemainMs              静音保留时长，需配合muteCutThreshold使用
         * @param enableLatexTn                是否播报latex公式，默认值：false，需将disableMarkdownFilter设为true
         * @param maxLengthToFilterParenthesis 是否过滤括号内的部分，0为不过滤，100为过滤，默认值：100
         * @param explicitLanguage             明确语种，仅读指定语种的文本
         * @param contextLanguage              参考语种，给模型提供参考的语种
         * @param unsupportedCharRatioThresh   不支持字符比例阈值，范围0.0~1.0，默认值：0.3
         * @param aigcWatermark                是否在合成结尾增加音频节奏标识，默认值：false
         * @param aigcMetadata                 元数据隐式水印配置
         * @param cacheConfig                  缓存相关参数配置
         * @param postProcess                  后处理配置
         * @param contextTexts                 语音合成的辅助信息，仅TTS2.0支持
         * @param sectionId                    其他合成语音的会话id，仅TTS2.0支持
         */
        @Builder
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Additions(
                @JsonProperty("silence_duration") Integer silenceDuration,
                @JsonProperty("enable_language_detector") Boolean enableLanguageDetector,
                @JsonProperty("disable_markdown_filter") Boolean disableMarkdownFilter,
                @JsonProperty("disable_emoji_filter") Boolean disableEmojiFilter,
                @JsonProperty("mute_cut_remain_ms") String muteCutRemainMs,
                @JsonProperty("enable_latex_tn") Boolean enableLatexTn,
                @JsonProperty("max_length_to_filter_parenthesis") Integer maxLengthToFilterParenthesis,
                @JsonProperty("explicit_language") String explicitLanguage,
                @JsonProperty("context_language") String contextLanguage,
                @JsonProperty("unsupported_char_ratio_thresh") Float unsupportedCharRatioThresh,
                @JsonProperty("aigc_watermark") Boolean aigcWatermark,
                @JsonProperty("aigc_metadata") AigcMetadata aigcMetadata,
                @JsonProperty("cache_config") CacheConfig cacheConfig,
                @JsonProperty("post_process") PostProcess postProcess,
                @JsonProperty("context_texts") List<String> contextTexts,
                @JsonProperty("section_id") String sectionId
        ) {
            public String toJsonString() {
                return JacksonUtil.toJsonString(this);
            }

            /**
             * AIGC元数据水印配置
             *
             * @param enable              是否启用隐式水印，默认值：false
             * @param contentProducer     合成服务提供者的名称或编码，默认值：""
             * @param produceId           内容制作编号，默认值：""
             * @param contentPropagator   内容传播服务提供者的名称或编码，默认值：""
             * @param propagateId         内容传播编号，默认值：""
             */
            @Builder
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public record AigcMetadata(
                    Boolean enable,
                    @JsonProperty("content_producer") String contentProducer,
                    @JsonProperty("produce_id") String produceId,
                    @JsonProperty("content_propagator") String contentPropagator,
                    @JsonProperty("propagate_id") String propagateId
            ) {}

            /**
             * 缓存配置参数
             *
             * @param textType 缓存文本类型，需要开启缓存时传1，默认值：1
             * @param useCache 是否使用缓存，需要开启缓存时传true，默认值：true
             */
            @Builder
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public record CacheConfig(
                    @JsonProperty("text_type") Integer textType,
                    @JsonProperty("use_cache") Boolean useCache
            ) {}

            /**
             * 后处理配置
             *
             * @param pitch 音调调节，取值范围[-12,12]，默认值：0
             */
            @Builder
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public record PostProcess(
                    Integer pitch
            ) {}
        }
    }
}
