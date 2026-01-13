package com.example.game.server.core.disruptor;

import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GameEventHandler implements EventHandler<GameEvent> {

    @Override
    public void onEvent(GameEvent event, long sequence, boolean endOfBatch) throws Exception {
        try {
            // 这里是单线程环境！放心操作内存中的 Room/Player 对象
            log.info("处理游戏逻辑: userId={}, msgType={}", event.getUserId(), event.getMsgType());

            // TODO: 根据 msgType 分发到具体的 Service (FSM状态机)
            // if (event.getMsgType() == 1001) loginService.handle(event);

        } catch (Exception e) {
            log.error("游戏逻辑处理异常", e);
        } finally {
            event.clear(); // 清理对象供 RingBuffer 复用
        }
    }
}