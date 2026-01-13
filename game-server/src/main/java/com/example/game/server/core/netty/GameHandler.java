package com.example.game.server.core.netty;

import com.example.game.common.proto.GameProto;
import com.example.game.server.core.disruptor.GameEvent;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@io.netty.channel.ChannelHandler.Sharable
@RequiredArgsConstructor
public class GameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

    private final Disruptor<GameEvent> disruptor;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) throws Exception {
        ByteBuf content = frame.content();
        // 1. Netty ByteBuf -> Java byte[]
        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);

        // 2. 反序列化外层 Packet (在IO线程做，如果包很大可以在逻辑线程做)
        GameProto.Packet packet = GameProto.Packet.parseFrom(bytes);
        int msgType = packet.getHeader().getMsgType();
        long userId = packet.getHeader().getUserId();

        log.info("网关收到消息: msgType={}, userId={}", msgType, userId);

        // 3. 丢入 Disruptor 队列 (非阻塞，极快)
        RingBuffer<GameEvent> ringBuffer = disruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        try {
            GameEvent event = ringBuffer.get(sequence);
            event.setChannel(ctx.channel());
            event.setMsgType(msgType);
            event.setUserId(userId);
            // 只把 body 传给逻辑层，header已经在上面处理了
            event.setData(packet.getBody().toByteArray());
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    // 增加连接建立和断开的日志，方便调试
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("客户端连接成功: {}", ctx.channel().id());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("客户端断开连接: {}", ctx.channel().id());
    }
}