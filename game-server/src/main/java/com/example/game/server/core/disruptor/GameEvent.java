package com.example.game.server.core.disruptor;

import io.netty.channel.Channel;
import lombok.Data;

@Data
public class GameEvent {
    private long userId;
    private int msgType;
    private byte[] data; // Protobuf 数据
    private Channel channel; // 玩家连接

    // 用于 Disruptor 复用对象后清理数据
    public void clear() {
        this.userId = 0;
        this.msgType = 0;
        this.data = null;
        this.channel = null;
    }
}