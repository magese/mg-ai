// app.js
document.addEventListener('DOMContentLoaded', function() {
  const chatContainer = document.getElementById('chatContainer');
  const messageInput = document.getElementById('messageInput');
  const sendButton = document.getElementById('sendButton');
  const clearButton = document.getElementById('clearButton');

  let currentEventSource = null;
  let isStreaming = false;
  let currentAudioChunks = []; // 存储当前响应的音频数据块
  let currentAudioMessageDiv = null; // 当前正在接收音频的消息div
  let currentAudioElement = null; // 当前正在播放的音频元素
  let audioQueue = []; // 音频播放队列
  let isPlaying = false; // 是否正在播放音频
  let audioTexts = []; // 存储音频文本
  let responseText = ''; // 主要响应文本
  let hasCreatedFinalMessage = false; // 标记是否已创建最终消息
  let allAudioChunks = []; // 存储所有音频数据，用于最终播放控件

  // 添加用户消息到聊天容器
  function addUserMessage(text) {
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message user-message';
    messageDiv.textContent = text;
    chatContainer.appendChild(messageDiv);
    scrollToBottom();
  }

  // 添加AI消息到聊天容器
  function addAiMessage(text, audioData = null) {
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message ai-message';

    // 主要响应文本
    const mainText = document.createElement('div');
    mainText.textContent = text;
    messageDiv.appendChild(mainText);

    // 如果有音频文本，添加音频文本（小字号）
    if (audioTexts.length > 0) {
      const audioTextDiv = document.createElement('div');
      audioTextDiv.className = 'audio-text';
      audioTextDiv.textContent = audioTexts.join(' ');
      messageDiv.appendChild(audioTextDiv);
    }

    // 如果有音频数据，添加音频播放控件
    if (audioData && audioData.length > 0) {
      console.log('创建音频控件，音频数据长度:', audioData.length);
      const audioControls = createAudioControls(audioData);
      messageDiv.appendChild(audioControls);

      // 添加音频指示器
      const audioIndicator = document.createElement('span');
      audioIndicator.className = 'audio-indicator';
      audioIndicator.title = '包含语音';
      messageDiv.appendChild(audioIndicator);
    } else {
      console.log('没有音频数据，跳过创建控件');
    }

    chatContainer.appendChild(messageDiv);
    scrollToBottom();
    return messageDiv;
  }

  // 创建AI消息流式响应元素
  function createStreamingMessage() {
    // 先检查是否已存在流式消息元素，避免重复创建
    const existingStreaming = document.getElementById('streaming-message');
    if (existingStreaming) {
      return existingStreaming;
    }

    const messageDiv = document.createElement('div');
    messageDiv.className = 'message ai-message';
    messageDiv.id = 'streaming-message';

    // 创建主要文本容器
    const mainText = document.createElement('div');
    mainText.id = 'streaming-main-text';
    messageDiv.appendChild(mainText);

    // 创建音频文本容器
    const audioText = document.createElement('div');
    audioText.className = 'audio-text';
    audioText.id = 'streaming-audio-text';
    messageDiv.appendChild(audioText);

    chatContainer.appendChild(messageDiv);
    scrollToBottom();
    isStreaming = true;
    currentAudioChunks = []; // 重置音频数据
    allAudioChunks = []; // 重置所有音频数据
    currentAudioMessageDiv = messageDiv; // 设置当前音频消息div
    audioTexts = []; // 重置音频文本
    responseText = ''; // 重置主要响应文本
    hasCreatedFinalMessage = false; // 重置标记
    return messageDiv;
  }

  // 更新流式响应消息
  function updateStreamingMessage(text) {
    const mainText = document.getElementById('streaming-main-text');
    if (mainText) {
      mainText.textContent = text;
      scrollToBottom();
    }
  }

  // 更新流式音频文本
  function updateStreamingAudioText(text) {
    const audioText = document.getElementById('streaming-audio-text');
    if (audioText) {
      // 避免重复添加相同的文本
      if (!audioTexts.includes(text)) {
        audioTexts.push(text);
        audioText.textContent = audioTexts.join(' ');
        scrollToBottom();
      }
    }
  }

  // 添加音频数据到当前流式消息
  function addAudioToStreamingMessage(audioData) {
    if (audioData && audioData.length > 0) {
      currentAudioChunks.push(audioData);
      allAudioChunks.push(audioData); // 同时添加到所有音频数据
    }
  }

  // 移除流式响应消息元素（但不删除DOM元素）
  function removeStreamingMessage() {
    const messageDiv = document.getElementById('streaming-message');
    if (messageDiv) {
      messageDiv.removeAttribute('id');
    }
    isStreaming = false;
  }

  // 完全删除流式响应消息元素
  function deleteStreamingMessage() {
    const messageDiv = document.getElementById('streaming-message');
    if (messageDiv) {
      messageDiv.remove();
    }
    isStreaming = false;
    currentAudioMessageDiv = null;
  }

  // 滚动到底部
  function scrollToBottom() {
    chatContainer.scrollTop = chatContainer.scrollHeight;
  }

  // 显示输入状态指示器
  function showTypingIndicator() {
    const indicatorDiv = document.createElement('div');
    indicatorDiv.className = 'typing-indicator';
    indicatorDiv.id = 'typing-indicator';

    for (let i = 0; i < 3; i++) {
      const dot = document.createElement('div');
      dot.className = 'typing-dot';
      indicatorDiv.appendChild(dot);
    }

    chatContainer.appendChild(indicatorDiv);
    scrollToBottom();
  }

  // 隐藏输入状态指示器
  function hideTypingIndicator() {
    const indicator = document.getElementById('typing-indicator');
    if (indicator) {
      indicator.remove();
    }
  }

  // 清理事件源
  function cleanupEventSource() {
    if (currentEventSource) {
      currentEventSource.close();
      currentEventSource = null;
    }
  }

  // 创建音频播放控件
  function createAudioControls(audioChunks) {
    const audioControls = document.createElement('div');
    audioControls.className = 'audio-controls';

    // 合并所有音频数据块
    const totalLength = audioChunks.reduce((total, chunk) => total + chunk.length, 0);
    const combinedAudio = new Uint8Array(totalLength);
    let offset = 0;
    audioChunks.forEach(chunk => {
      combinedAudio.set(chunk, offset);
      offset += chunk.length;
    });

    // 创建Blob URL
    const audioBlob = new Blob([combinedAudio], { type: 'audio/mp3' });
    const audioUrl = URL.createObjectURL(audioBlob);

    // 创建音频元素
    const audio = new Audio(audioUrl);

    // 播放按钮
    const playButton = document.createElement('button');
    playButton.className = 'play-button';
    playButton.textContent = '播放';

    // 进度条容器
    const progressContainer = document.createElement('div');
    progressContainer.className = 'audio-progress';
    const progressBar = document.createElement('div');
    progressBar.className = 'audio-progress-bar';
    progressContainer.appendChild(progressBar);

    // 时间显示
    const timeDisplay = document.createElement('div');
    timeDisplay.className = 'audio-time';
    timeDisplay.textContent = '0:00';

    audioControls.appendChild(playButton);
    audioControls.appendChild(progressContainer);
    audioControls.appendChild(timeDisplay);

    // 音频事件处理
    audio.addEventListener('loadedmetadata', () => {
      console.log('音频已加载，时长:', audio.duration);
      timeDisplay.textContent = formatTime(audio.duration);
    });

    audio.addEventListener('timeupdate', () => {
      if (audio.duration) {
        const progress = (audio.currentTime / audio.duration) * 100;
        progressBar.style.width = progress + '%';
        timeDisplay.textContent = formatTime(audio.currentTime) + ' / ' + formatTime(audio.duration);
      }
    });

    audio.addEventListener('ended', () => {
      playButton.textContent = '播放';
      progressBar.style.width = '0%';
      if (audio.duration) {
        timeDisplay.textContent = formatTime(audio.duration);
      }
    });

    audio.addEventListener('pause', () => {
      playButton.textContent = '播放';
    });

    playButton.addEventListener('click', () => {
      if (audio.paused) {
        audio.play().then(() => {
          playButton.textContent = '暂停';
        }).catch(e => {
          console.error('播放失败:', e);
        });
      } else {
        audio.pause();
        playButton.textContent = '播放';
      }
    });

    // 清理URL当页面卸载时
    window.addEventListener('beforeunload', () => {
      URL.revokeObjectURL(audioUrl);
    });

    return audioControls;
  }

  // 播放音频队列
  function playAudioQueue() {
    if (isPlaying || audioQueue.length === 0) return;

    isPlaying = true;

    const playNext = () => {
      if (audioQueue.length === 0) {
        isPlaying = false;
        return;
      }

      const {audioData, text} = audioQueue.shift();

      // 更新音频文本显示
      if (text && !audioTexts.includes(text)) {
        updateStreamingAudioText(text);
      }

      // 合并所有音频数据块
      const totalLength = audioData.reduce((total, chunk) => total + chunk.length, 0);
      const combinedAudio = new Uint8Array(totalLength);
      let offset = 0;
      audioData.forEach(chunk => {
        combinedAudio.set(chunk, offset);
        offset += chunk.length;
      });

      // 创建Blob URL
      const audioBlob = new Blob([combinedAudio], { type: 'audio/mp3' });
      const audioUrl = URL.createObjectURL(audioBlob);

      // 停止当前播放的音频
      if (currentAudioElement) {
        currentAudioElement.pause();
        URL.revokeObjectURL(currentAudioElement.src);
      }

      // 创建并播放新的音频
      currentAudioElement = new Audio(audioUrl);
      currentAudioElement.play().catch(e => {
        console.error('自动播放失败:', e);
      });

      // 音频播放结束后继续播放下一个
      currentAudioElement.addEventListener('ended', () => {
        URL.revokeObjectURL(audioUrl);
        playNext();
      });
    };

    playNext();
  }

  // 添加音频到播放队列
  function addAudioToQueue(audioData, text) {
    audioQueue.push({audioData, text});

    // 如果没有正在播放，开始播放队列
    if (!isPlaying) {
      playAudioQueue();
    }
  }

  // 格式化时间显示
  function formatTime(seconds) {
    if (isNaN(seconds) || seconds === Infinity) return '0:00';
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }

  // 发送消息到服务器
  function sendMessage() {
    const message = messageInput.value.trim();
    if (!message) return;

    // 禁用输入和发送按钮
    messageInput.disabled = true;
    sendButton.disabled = true;

    // 添加用户消息
    addUserMessage(message);

    // 清空输入框
    messageInput.value = '';

    // 显示输入状态指示器
    showTypingIndicator();

    // 创建流式消息元素
    createStreamingMessage();

    // 发送请求到服务器
    const url = `http://127.0.0.1:15977/v2/chat?message=${encodeURIComponent(message)}`;

    // 使用EventSource接收服务器发送的事件流
    cleanupEventSource();
    const eventSource = new EventSource(url);
    currentEventSource = eventSource;

    let hasReceivedData = false;

    eventSource.onmessage = function(event) {
      hideTypingIndicator();
      hasReceivedData = true;

      try {
        const eventData = JSON.parse(event.data);

        switch (eventData.type) {
          case 'TEXT':
            // 累积响应文本
            responseText += eventData.text;
            updateStreamingMessage(responseText);
            break;

          case 'AUDIO':
            // 处理音频数据
            if (eventData.audioData) {
              // 将base64音频数据转换为Uint8Array
              const audioData = base64ToUint8Array(eventData.audioData);
              addAudioToStreamingMessage(audioData);
            }
            break;

          case 'PLAY':
            // 播放事件：将音频添加到播放队列
            // 注意：PLAY事件的text只用于音频文本，不拼接到主要文本
            if (eventData.text) {
              updateStreamingAudioText(eventData.text);
            }

            // 将当前收集的音频添加到播放队列
            if (currentAudioChunks.length > 0) {
              addAudioToQueue([...currentAudioChunks], eventData.text);
              currentAudioChunks = []; // 清空当前音频数据，准备接收下一段
            }
            break;

          case 'END':
            // 结束事件，完成消息处理
            console.log('收到结束事件');
            eventSource.close();

            // 确保只创建一次最终消息
            if (!hasCreatedFinalMessage) {
              hasCreatedFinalMessage = true;

              // 将流式消息转为普通AI消息，包含所有音频数据
              const finalMessageDiv = addAiMessage(responseText, allAudioChunks);
              currentAudioMessageDiv = finalMessageDiv;

              // 移除流式消息
              deleteStreamingMessage();

              // 恢复界面
              messageInput.disabled = false;
              sendButton.disabled = false;
              messageInput.focus();
            }
            break;

          default:
            console.warn('未知事件类型:', eventData.type);
        }
      } catch (e) {
        console.error('解析事件数据失败:', e, event);
      }
    };

    eventSource.onerror = function(event) {
      console.log('EventSource连接已关闭', event);
      hideTypingIndicator();

      // 重新启用输入和发送按钮
      messageInput.disabled = false;
      sendButton.disabled = false;
      messageInput.focus();

      // 确保只创建一次最终消息
      if (!hasCreatedFinalMessage) {
        hasCreatedFinalMessage = true;

        if (hasReceivedData && responseText) {
          // 有收到数据：将流式消息转为普通AI消息
          const finalMessageDiv = addAiMessage(responseText, allAudioChunks);
          currentAudioMessageDiv = finalMessageDiv;
        } else {
          // 没有收到数据：移除流式消息并显示错误
          deleteStreamingMessage();
          addAiMessage('抱歉，没有收到服务器的响应。');
        }
      }

      cleanupEventSource();
    };
  }

  // Base64转Uint8Array
  function base64ToUint8Array(base64) {
    const binaryString = atob(base64);
    const bytes = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) {
      bytes[i] = binaryString.charCodeAt(i);
    }
    return bytes;
  }

  // 清空聊天记录
  function clearChat() {
    while (chatContainer.firstChild) {
      chatContainer.removeChild(chatContainer.firstChild);
    }

    cleanupEventSource();
    isStreaming = false;
    currentAudioChunks = [];
    allAudioChunks = [];
    currentAudioMessageDiv = null;
    audioQueue = [];
    isPlaying = false;
    audioTexts = [];
    responseText = '';
    hasCreatedFinalMessage = false;

    // 停止当前播放的音频
    if (currentAudioElement) {
      currentAudioElement.pause();
      currentAudioElement = null;
    }

    messageInput.disabled = false;
    sendButton.disabled = false;
    messageInput.focus();

    const welcomeMessage = document.createElement('div');
    welcomeMessage.className = 'welcome-message';
    welcomeMessage.textContent = '您好！我是龟龟，你好吗？';
    chatContainer.appendChild(welcomeMessage);
  }

  // 事件监听
  sendButton.addEventListener('click', sendMessage);

  messageInput.addEventListener('keypress', function(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  });

  clearButton.addEventListener('click', clearChat);

  // 初始聚焦到输入框
  messageInput.focus();
});
