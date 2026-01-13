package com.example.game.server.core.disruptor;

import com.example.game.server.core.disruptor.GameEvent;
import com.example.game.server.core.disruptor.GameEventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DisruptorConfig {

    @Bean
    public Disruptor<GameEvent> gameDisruptor(GameEventHandler eventHandler) {
        // RingBuffer 大小必须是 2 的 N 次方
        int bufferSize = 1024 * 64;

        Disruptor<GameEvent> disruptor = new Disruptor<>(
                GameEvent::new,
                bufferSize,
                DaemonThreadFactory.INSTANCE
        );

        // 绑定消费者
        disruptor.handleEventsWith(eventHandler);
        disruptor.start();
        return disruptor;
    }
}