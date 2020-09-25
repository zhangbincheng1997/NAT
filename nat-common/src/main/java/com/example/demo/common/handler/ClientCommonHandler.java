package com.example.demo.common.handler;

import com.example.demo.common.protocol.Message;
import com.example.demo.common.protocol.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ClientCommonHandler<T> extends SimpleChannelInboundHandler<T> {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            // 客户端负责发送心跳包
            if (e.state() == IdleState.WRITER_IDLE) {
                log.info("心跳检测...");
                Message message = Message.of(MessageType.KEEPALIVE);
                ctx.writeAndFlush(message);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("捕获异常...", cause);
        ctx.close();
    }
}
