package com.magese.ai.mcpagent.client.tts;

import cn.hutool.core.util.IdUtil;
import com.magese.ai.mcpagent.client.tts.domain.VolTTSWsRequest;
import com.magese.ai.mcpagent.client.tts.domain.VolTTSWsSentenceEndPayload;
import com.magese.ai.mcpagent.client.tts.protocol.EventType;
import com.magese.ai.mcpagent.client.tts.protocol.Message;
import com.magese.ai.mcpagent.client.tts.protocol.MsgType;
import com.magese.ai.mcpagent.domain.AudioChunk;
import com.magese.ai.mcpagent.util.JacksonUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * TTS会话管理
 *
 * @author Magese
 * @since 2025/10/15 09:40
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class VolcTTSSessionManager {

    private final VolcTTSProperties volcTTSProperties;
    private final VolcTTSWebSocketClient volcTTSWebSocketClient;

    @Getter
    private volatile String currentSessionId;
    private volatile boolean sessionStarted = false;


    /**
     * 合成文本为语音
     */
    public Flux<AudioChunk> synthesizeText(String text) {
        return Flux.create(sink -> {
            try {
                if (!sessionStarted) {
                    // 第一次调用时建立连接并开启会话
                    startSession();
                }
                // 发送文本到 TTS 服务
                sendTextToSession(text);

                // 处理接收到的音频数据
                processIncomingAudio(sink);

                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    /**
     * 结束会话并返回剩余音频
     */
    public Flux<AudioChunk> endSession() {
        return Flux.create(sink -> {
            try {
                if (sessionStarted && currentSessionId != null) {
                    // 发送结束会话请求
                    endSessionRequest();

                    // 处理剩余音频数据直到收到 SESSION_FINISHED
                    processRemainingAudio(sink);

                    sessionStarted = false;
                    currentSessionId = null;
                    log.info("TTS session ended successfully");
                } else {
                    sink.error(new IllegalStateException("No active session to end"));
                }
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    /**
     * 强制结束当前会话（用于清理资源）
     */
    public void forceEndSession() {
        if (sessionStarted && currentSessionId != null) {
            try {
                endSessionRequest();
                // 不处理剩余音频，直接结束
                sessionStarted = false;
                currentSessionId = null;
                log.info("TTS session force ended");
            } catch (Exception e) {
                log.error("Error force ending TTS session", e);
            }
        }
    }

    /**
     * 检查当前是否有活跃会话
     */
    public boolean hasActiveSession() {
        return sessionStarted && currentSessionId != null;
    }

    /**
     * 开始 TTS 会话
     */
    private void startSession() {
        try {
            if (sessionStarted) {
                throw new IllegalStateException("TTS session already started");
            }

            establishConnection();

            currentSessionId = IdUtil.fastUUID();
            VolTTSWsRequest startRequest = buildTTSRequest(null, EventType.START_SESSION);
            volcTTSWebSocketClient.sendStartSession(JacksonUtil.toJsonBytes(startRequest), currentSessionId);
            volcTTSWebSocketClient.waitForMessage(MsgType.FULL_SERVER_RESPONSE, EventType.SESSION_STARTED);
            sessionStarted = true;
            log.info("TTS session started: {}", currentSessionId);
        } catch (Exception e) {
            log.error("Failed to start TTS session", e);
            throw new RuntimeException("Failed to start TTS session", e);
        }
    }

    /**
     * 发送文本到当前会话
     */
    private void sendTextToSession(String text) {
        if (!sessionStarted) {
            throw new IllegalStateException("No active TTS session");
        }
        VolTTSWsRequest request = buildTTSRequest(text, EventType.TASK_REQUEST);
        volcTTSWebSocketClient.sendTaskRequest(JacksonUtil.toJsonBytes(request), currentSessionId);
        log.debug("Sent text to TTS session: {} characters", text.length());
    }

    /**
     * 处理接收到的音频数据
     */
    private void processIncomingAudio(FluxSink<AudioChunk> sink) {
        try {
            // 非阻塞方式检查是否有可用的音频数据
            Message msg = volcTTSWebSocketClient.receiveMessageImmediately();
            if (msg != null && msg.getPayload() != null) {
                switch (msg.getType()) {
                    case FULL_SERVER_RESPONSE:
                        if (msg.getEvent() == EventType.TTS_SENTENCE_END) {
                            VolTTSWsSentenceEndPayload payload = JacksonUtil.parseObject(msg.getPayload(), VolTTSWsSentenceEndPayload.class);
                            sink.next(new AudioChunk(null, payload.text(), volcTTSProperties.getEncoding()));
                        }
                        break;
                    case AUDIO_ONLY_SERVER:
                        AudioChunk chunk = new AudioChunk(msg.getPayload(), null, volcTTSProperties.getEncoding());
                        sink.next(chunk);
                        break;
                }
            }
        } catch (Exception e) {
            log.warn("Error processing audio chunk", e);
        }
    }

    /**
     * 在结束会话后处理剩余音频数据
     */
    private void processRemainingAudio(FluxSink<AudioChunk> sink) {
        try {
            boolean sessionFinished = false;
            while (!sink.isCancelled() && !sessionFinished) {
                Message msg = volcTTSWebSocketClient.receiveMessage();
                switch (msg.getType()) {
                    case FULL_SERVER_RESPONSE:
                        if (msg.getEvent() == EventType.TTS_SENTENCE_END) {
                            VolTTSWsSentenceEndPayload payload = JacksonUtil.parseObject(msg.getPayload(), VolTTSWsSentenceEndPayload.class);
                            sink.next(new AudioChunk(null, payload.text(), volcTTSProperties.getEncoding()));
                        }
                        break;
                    case AUDIO_ONLY_SERVER:
                        if (msg.getPayload() != null) {
                            AudioChunk chunk = new AudioChunk(msg.getPayload(), null, volcTTSProperties.getEncoding());
                            sink.next(chunk);
                            log.debug("Received remaining audio chunk: {} bytes", msg.getPayload().length);
                        }
                        break;
                    default:
                        log.warn("Unexpected message after session end: {}", msg.getType());
                }

                if (msg.getEvent() == EventType.SESSION_FINISHED) {
                    sessionFinished = true;
                    log.info("TTS session finished successfully");
                }
            }
        } catch (Exception e) {
            if (!sink.isCancelled()) {
                log.error("Error processing remaining audio", e);
                sink.error(e);
            }
        }
    }

    /**
     * 建立连接
     */
    private void establishConnection() {
        volcTTSWebSocketClient.sendStartConnection();
        volcTTSWebSocketClient.waitForMessage(MsgType.FULL_SERVER_RESPONSE, EventType.CONNECTION_STARTED);
    }

    /**
     * 发送结束会话请求
     */
    private void endSessionRequest() {
        if (!sessionStarted || currentSessionId == null) {
            throw new IllegalStateException("No active session to end");
        }
        volcTTSWebSocketClient.sendFinishSession(currentSessionId);
        log.debug("Sent end session request");
    }

    /**
     * 构造TTS请求
     *
     * @param text  语音文本
     * @param event 事件类型
     * @return 语音流
     */
    private VolTTSWsRequest buildTTSRequest(String text, EventType event) {
        return VolTTSWsRequest.builder()
                .user(new VolTTSWsRequest.User(IdUtil.nanoId()))
                .namespace("BidirectionalTTS")
                .event(event != null ? event.getValue() : null)
                .reqParams(VolTTSWsRequest.ReqParams.builder()
                        .text(text)
                        .speaker(volcTTSProperties.getVoice())
                        .audioParams(
                                VolTTSWsRequest.ReqParams.AudioParams.builder()
                                        .format(volcTTSProperties.getEncoding())
                                        .bitRate(24000)
                                        .speechRate(5)
                                        .enableTimestamp(true)
                                        .build()
                        )
                        .additions(VolTTSWsRequest.ReqParams.Additions.builder()
                                .enableLanguageDetector(true)
                                .maxLengthToFilterParenthesis(50)
                                .build()
                                .toJsonString()
                        )
                        .build()
                )
                .build();
    }
}
