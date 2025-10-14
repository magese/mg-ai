package com.magese.ai.mcpagent.client.tts;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import com.magese.ai.mcpagent.client.tts.protocol.EventType;
import com.magese.ai.mcpagent.client.tts.protocol.Message;
import com.magese.ai.mcpagent.client.tts.protocol.MsgType;
import com.magese.ai.mcpagent.client.tts.protocol.MsgTypeFlagBits;
import com.magese.ai.mcpagent.util.JacksonUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
@Slf4j
@RequiredArgsConstructor
@Component
public class VolcTTSWebSocketClient {

    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private static final long RECONNECT_DELAY_MS = 5000;
    private static final int QUEUE_TIMEOUT_SECONDS = 30;

    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    private final VolcTTSProperties volcTTSProperties;
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);
    private final AtomicBoolean isActive = new AtomicBoolean(false);
    private final Object connectionLock = new Object();

    private WebSocketClient client;
    private ScheduledExecutorService reconnectScheduler;

    @PostConstruct
    public void init() {
        reconnectScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "tts-websocket-reconnect");
            thread.setDaemon(true);
            return thread;
        });
        initializeAndConnect();
    }

    @PreDestroy
    public void destroy() {
        log.info("正在销毁WebSocket客户端...");
        disconnect();

        if (reconnectScheduler != null) {
            try {
                reconnectScheduler.shutdown();
                if (!reconnectScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    reconnectScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                reconnectScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void initializeAndConnect() {
        synchronized (connectionLock) {
            if (isConnecting.get() || isActive.get()) {
                return;
            }

            try {
                isConnecting.set(true);

                // 关闭现有连接（如果有）
                if (client != null) {
                    try {
                        client.close();
                    } catch (Exception e) {
                        log.debug("关闭现有WebSocket连接时发生异常", e);
                    }
                    client = null;
                }

                URI endpoint = new URI(volcTTSProperties.getEndpoint());
                Map<String, String> headers = createHeaders();

                client = new WebSocketClient(endpoint, headers) {
                    @Override
                    public void onOpen(ServerHandshake handshake) {
                        String logId = handshake.getFieldValue("X-Tt-Logid");
                        log.info("火山云语音合成WebSocket连接已建立，状态码: {}，X-Tt-Logid：{}", handshake.getHttpStatus(), logId);
                        reconnectAttempts.set(0);
                        isActive.set(true);
                        isConnecting.set(false);
                    }

                    @Override
                    public void onMessage(String message) {
                        log.warn("火山云语音合成接收到不期望的文本消息：{}", message);
                    }

                    @Override
                    public void onMessage(ByteBuffer bytes) {
                        try {
                            log.info("火山云语音合成接收消息：{}", bytes.array().length);
                            Message message = Message.unmarshal(bytes.array());
                            boolean offered = messageQueue.offer(message, QUEUE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                            if (!offered) {
                                log.warn("消息队列已满，无法处理新消息");
                            }
                        } catch (Exception e) {
                            log.error("火山云语音合成解析二进制消息异常", e);
                        }
                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {
                        log.info("火山云语音合成WebSocket连接已关闭：code={}, reason={}, remote={}", code, reason, remote);
                        isActive.set(false);
                        isConnecting.set(false);

                        // 如果不是主动关闭，尝试重连
                        if (code != 1000 && reconnectAttempts.get() < MAX_RECONNECT_ATTEMPTS) {
                            scheduleReconnect();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        log.error("火山云语音合成WebSocket连接异常", e);
                        isConnecting.set(false);
                    }
                };

                log.info("正在连接火山云语音合成WebSocket，连接地址：{}，请求头信息：{}", endpoint, JacksonUtil.toJsonString(headers));
                client.connect();

            } catch (Exception e) {
                log.error("初始化火山云语音合成WebSocket客户端失败", e);
                isConnecting.set(false);
                scheduleReconnect();
            }
        }
    }

    private Map<String, String> createHeaders() {
        return Map.of(
                "X-Api-App-Key", volcTTSProperties.getAppId(),
                "X-Api-Access-Key", volcTTSProperties.getAccessToken(),
                "X-Api-Resource-Id", "seed-tts-1.0",
                "X-Api-Connect-Id", IdUtil.fastUUID());
    }

    private void scheduleReconnect() {
        int attempts = reconnectAttempts.incrementAndGet();
        if (attempts <= MAX_RECONNECT_ATTEMPTS) {
            long delay = RECONNECT_DELAY_MS * attempts;
            log.info("计划在 {}ms 后尝试第 {} 次重连", delay, attempts);
            reconnectScheduler.schedule(this::initializeAndConnect, delay, TimeUnit.MILLISECONDS);
        } else {
            log.error("已达到最大重连次数 ({})，停止重连", MAX_RECONNECT_ATTEMPTS);
        }
    }

    /**
     * 连接WebSocket
     */
    public void connect() {
        if (!isActive.get() && !isConnecting.get()) {
            initializeAndConnect();
        }
    }

    /**
     * 关闭连接
     */
    public void disconnect() {
        synchronized (connectionLock) {
            log.info("正在关闭WebSocket连接...");
            isActive.set(false);
            isConnecting.set(false);
            reconnectAttempts.set(MAX_RECONNECT_ATTEMPTS); // 防止重连

            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                    log.error("关闭WebSocket连接时发生异常", e);
                } finally {
                    client = null;
                }
            }
            // 清空消息队列
            messageQueue.clear();
        }
    }


    public void sendStartConnection() {
        Message message = new Message(MsgType.FULL_CLIENT_REQUEST, MsgTypeFlagBits.WITH_EVENT);
        message.setEvent(EventType.START_CONNECTION);
        message.setPayload("{}".getBytes());
        sendMessage(message);
    }

    public void sendFinishConnection() {
        Message message = new Message(MsgType.FULL_CLIENT_REQUEST, MsgTypeFlagBits.WITH_EVENT);
        message.setEvent(EventType.FINISH_CONNECTION);
        sendMessage(message);
    }

    public void sendStartSession(byte[] payload, String sessionId) {
        Message message = new Message(MsgType.FULL_CLIENT_REQUEST, MsgTypeFlagBits.WITH_EVENT);
        message.setEvent(EventType.START_SESSION);
        message.setSessionId(sessionId);
        message.setPayload(payload);
        sendMessage(message);
    }

    public void sendFinishSession(String sessionId) {
        Message message = new Message(MsgType.FULL_CLIENT_REQUEST, MsgTypeFlagBits.WITH_EVENT);
        message.setEvent(EventType.FINISH_SESSION);
        message.setSessionId(sessionId);
        message.setPayload("{}".getBytes());
        sendMessage(message);
    }

    public void sendTaskRequest(byte[] payload, String sessionId) {
        Message message = new Message(MsgType.FULL_CLIENT_REQUEST, MsgTypeFlagBits.WITH_EVENT);
        message.setEvent(EventType.TASK_REQUEST);
        message.setSessionId(sessionId);
        message.setPayload(payload);
        sendMessage(message);
    }

    public void sendFullClientMessage(byte[] payload) {
        Message message = new Message(MsgType.FULL_CLIENT_REQUEST, MsgTypeFlagBits.NO_SEQ);
        message.setPayload(payload);
        sendMessage(message);
    }

    @SneakyThrows
    public void sendMessage(Message message) {
        if (!isActive.get()) {
            log.warn("WebSocket连接未激活，尝试重新连接");
            connect();
            // 等待连接建立
            awaitConnection();
        }

        try {
            if (client != null && isActive.get()) {
                log.info("语音合成发送：{}", message);
                client.send(message.marshal());
            } else {
                throw new IllegalStateException("WebSocket连接不可用，无法发送消息");
            }
        } catch (Exception e) {
            log.error("发送消息失败", e);
            throw new RuntimeException("发送WebSocket消息失败", e);
        }
    }

    private void awaitConnection() {
        int maxWaitTime = 5000; // 最多等待5秒
        int waitInterval = 100; // 每次等待100毫秒
        int waitedTime = 0;

        while (!isActive.get() && waitedTime < maxWaitTime) {
            if (ThreadUtil.sleep(waitInterval)) {
                waitedTime += waitInterval;
            } else {
                throw new RuntimeException("等待连接建立时被中断");
            }
        }
        if (!isActive.get()) {
            throw new IllegalStateException("等待连接建立超时");
        }
    }

    @SneakyThrows
    public Message receiveMessage() {
        try {
            Message message = messageQueue.poll(QUEUE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (message == null) {
                throw new TimeoutException("接收消息超时");
            }
            log.info("语音合成接收：{}", message);
            return message;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("接收消息被中断", e);
        } catch (TimeoutException e) {
            throw new RuntimeException("接收消息超时", e);
        }
    }

    public Message waitForMessage(MsgType type, EventType event) {
        Message message = receiveMessage();
        if (message.getType() == type && message.getEvent() == event) {
            return message;
        } else {
            throw new RuntimeException("Unexpected message: " + message);
        }
    }
}
