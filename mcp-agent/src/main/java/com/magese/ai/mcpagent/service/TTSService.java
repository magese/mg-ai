package com.magese.ai.mcpagent.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.magese.ai.mcpagent.client.tts.VolcTTSProperties;
import com.magese.ai.mcpagent.client.tts.VolcTTSWebSocketClient;
import com.magese.ai.mcpagent.client.tts.domain.VolTTSWsRequest;
import com.magese.ai.mcpagent.client.tts.domain.VolTTSWsResult;
import com.magese.ai.mcpagent.client.tts.protocol.EventType;
import com.magese.ai.mcpagent.client.tts.protocol.Message;
import com.magese.ai.mcpagent.client.tts.protocol.MsgType;
import com.magese.ai.mcpagent.constant.Consts;
import com.magese.ai.mcpagent.util.JacksonUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * 语音处理Service
 *
 * @author Magese
 * @since 2025/10/11 16:45
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TTSService {

    private final VolcTTSWebSocketClient VolcTTSWebSocketClient;
    private final VolcTTSProperties volcTTSProperties;

    /**
     * 合成语音
     *
     * @param request 语音合成请求
     * @return 语音流
     */
    public VolTTSWsResult synthesizeSpeech(VolTTSWsRequest request) {
        VolTTSWsRequest.ReqParams reqParams = request.reqParams();

        String voice = StrUtil.isBlank(reqParams.speaker()) ? volcTTSProperties.getVoice() : reqParams.speaker();
        String encoding = StrUtil.isBlank(reqParams.audioParams().format()) ? volcTTSProperties.getEncoding() : reqParams.audioParams().format();

        // Start connection
        VolcTTSWebSocketClient.sendStartConnection();
        // Wait for connection started
        VolcTTSWebSocketClient.waitForMessage(MsgType.FULL_SERVER_RESPONSE, EventType.CONNECTION_STARTED);
        String sessionId = IdUtil.fastUUID();
        // Start session
        VolTTSWsRequest startRequest = request.withEvent(EventType.START_SESSION.getValue())
                .withReqParams(reqParams.withText(null));
        VolcTTSWebSocketClient.sendStartSession(JacksonUtil.toJsonBytes(startRequest), sessionId);
        // Wait for session started
        Message message = VolcTTSWebSocketClient.waitForMessage(MsgType.FULL_SERVER_RESPONSE, EventType.SESSION_STARTED);
        log.info("TTS会话已建立：{}", message);

        // 发送请求
        // Process each sentence
        // Send text
        String text = reqParams.text();
        for (char c : text.toCharArray()) {
            // Create current request
            VolTTSWsRequest currentRequest = request.withEvent(EventType.TASK_REQUEST.getValue())
                    .withReqParams(reqParams.withText(String.valueOf(c)));
            VolcTTSWebSocketClient.sendTaskRequest(JacksonUtil.toJsonBytes(currentRequest), sessionId);
        }

        // End session
        VolcTTSWebSocketClient.sendFinishSession(sessionId);

        // 接收响应
        ByteArrayOutputStream audioStream = receiveMessage();
        // 保存音频文件
        File audioFile = saveAudioFile(audioStream, voice, encoding);
        return new VolTTSWsResult(audioStream, voice, encoding, audioFile.getName(), audioFile.length());
    }

    /**
     * 合成语音
     *
     * @param text  语音文本
     * @param voice 语音音色
     * @return 语音流
     */
    public VolTTSWsResult synthesizeSpeech(String text, String voice) {
        VolTTSWsRequest request = VolTTSWsRequest.builder()
                .user(new VolTTSWsRequest.User(IdUtil.nanoId()))
                .namespace("BidirectionalTTS")
                .reqParams(VolTTSWsRequest.ReqParams.builder()
                        .text(text)
                        .speaker(voice.isEmpty() ? volcTTSProperties.getVoice() : voice)
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
        return synthesizeSpeech(request);
    }

    /**
     * 接收消息
     */
    @SneakyThrows
    private ByteArrayOutputStream receiveMessage() {
        boolean audioReceived = false;
        ByteArrayOutputStream audioStream = new ByteArrayOutputStream();
        while (true) {
            Message msg = VolcTTSWebSocketClient.receiveMessage();
            switch (msg.getType()) {
                case FULL_SERVER_RESPONSE:
                    break;
                case AUDIO_ONLY_SERVER:
                    if (!audioReceived && audioStream.size() > 0) {
                        audioReceived = true;
                    }
                    if (msg.getPayload() != null) {
                        audioStream.write(msg.getPayload());
                    }
                    break;
                default:
                    throw new RuntimeException("Unexpected message: " + msg);
            }
            if (msg.getEvent() == EventType.SESSION_FINISHED) {
                break;
            }
        }

        if (!audioReceived) {
            throw new RuntimeException("No audio data received");
        }
        return audioStream;
    }

    /**
     * 保存音频文件
     *
     * @param audioStream 音频文件流
     * @param voice       语音音色
     * @param encoding    文件编码
     */
    private File saveAudioFile(ByteArrayOutputStream audioStream, String voice, String encoding) {
        String time = LocalDateTimeUtil.format(LocalDateTimeUtil.now(), DatePattern.PURE_DATETIME_FORMATTER);
        String fileName = String.format("%s-%s.%s", voice, time, encoding);
        String filePath = Consts.LOCAL_FILE_DIR + "audios/" + fileName;
        File file = FileUtil.writeBytes(audioStream.toByteArray(), filePath);
        log.info("音频文件已保存：{}，文件大小：{}", file.getPath(), FileUtil.readableFileSize(file));
        return file;
    }
}
