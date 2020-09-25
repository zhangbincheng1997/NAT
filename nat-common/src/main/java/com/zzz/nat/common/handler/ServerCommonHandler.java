package com.zzz.nat.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ServerCommonHandler<T> extends SimpleChannelInboundHandler<T> {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            // 服务端负责心跳检测
            if (e.state() == IdleState.READER_IDLE) {
                log.info("一段时间内没有数据接收：{}", ctx.channel().id());
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("捕获异常...", cause);
        ctx.close();
    }
}
