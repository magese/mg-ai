package com.magese.ai.mcpagent.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import com.magese.ai.mcpagent.client.tts.VolcTTSProperties;
import com.magese.ai.mcpagent.client.tts.VolcTTSSessionManager;
import com.magese.ai.mcpagent.client.tts.domain.VolTTSWsResult;
import com.magese.ai.mcpagent.constant.Consts;
import com.magese.ai.mcpagent.domain.AudioChunk;
import com.magese.ai.mcpagent.domain.ChatEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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


    private final VolcTTSProperties volcTTSProperties;
    private final VolcTTSSessionManager volcTTSSessionManager;


    /**
     * 合成语音
     *
     * @param text 语音文本
     * @return 语音流
     */
    public VolTTSWsResult synthesizeSpeech(String text) {
        // 检查是否有活跃会话
        if (hasActiveSession()) {
            throw new IllegalStateException("TTS service is busy with session: " + getCurrentSessionId());
        }

        String voice = volcTTSProperties.getVoice();
        String encoding = volcTTSProperties.getEncoding();
        List<AudioChunk> chunks = new ArrayList<>();

        try {
            // 使用阻塞方式收集所有音频块
            volcTTSSessionManager.synthesizeText(text)
                    .doOnNext(chunks::add)
                    .thenMany(volcTTSSessionManager.endSession())
                    .doOnNext(chunks::add)
                    .blockLast(); // 阻塞直到所有数据接收完成

            // 合并所有音频数据
            byte[] audioBytes = combineAudioChunks(chunks);
            File file = saveAudioFile(audioBytes, voice, encoding);
            return new VolTTSWsResult(audioBytes, voice, encoding, file.getName(), file.length());

        } catch (Exception e) {
            log.error("Error in synchronous TTS synthesis", e);
            volcTTSSessionManager.forceEndSession();
            throw new RuntimeException("TTS synthesis failed", e);
        }
    }

    /**
     * 合并多个音频块为一个连续的音频数据
     */
    private byte[] combineAudioChunks(List<AudioChunk> chunks) {
        if (chunks.isEmpty()) {
            return new byte[0];
        }

        // 计算总长度
        int totalLength = chunks.stream()
                .mapToInt(chunk -> chunk.data().length)
                .sum();

        // 合并所有数据
        byte[] combined = new byte[totalLength];
        int currentPosition = 0;

        for (AudioChunk chunk : chunks) {
            byte[] chunkData = chunk.data();
            System.arraycopy(chunkData, 0, combined, currentPosition, chunkData.length);
            currentPosition += chunkData.length;
        }

        log.debug("Combined {} audio chunks into {} bytes", chunks.size(), totalLength);
        return combined;
    }

    /**
     * 合成语音
     *
     * @return 语音流
     */
    public Flux<AudioChunk> synthesizeSpeechStream(String text) {
        return volcTTSSessionManager.synthesizeText(text);
    }

    /**
     * 结束会话
     */
    public Flux<AudioChunk> endSession() {
        return volcTTSSessionManager.endSession();
    }

    /**
     * 强制结束会话（用于清理）
     */
    public void forceEndSession() {
        volcTTSSessionManager.forceEndSession();
    }

    /**
     * 检查是否有活跃会话
     */
    public boolean hasActiveSession() {
        return volcTTSSessionManager.hasActiveSession();
    }

    /**
     * 获取当前会话ID
     */
    public String getCurrentSessionId() {
        return volcTTSSessionManager.getCurrentSessionId();
    }

    /**
     * 保存音频文件
     *
     * @param audioBytes 音频文件流
     * @param voice      语音音色
     * @param encoding   文件编码
     */
    private File saveAudioFile(byte[] audioBytes, String voice, String encoding) {
        String time = LocalDateTimeUtil.format(LocalDateTimeUtil.now(), DatePattern.PURE_DATETIME_FORMATTER);
        String fileName = String.format("%s-%s.%s", voice, time, encoding);
        String filePath = Consts.LOCAL_FILE_DIR + "audios/" + fileName;
        File file = FileUtil.writeBytes(audioBytes, filePath);
        log.info("音频文件已保存：{}，文件大小：{}", file.getPath(), FileUtil.readableFileSize(file));
        return file;
    }
}
