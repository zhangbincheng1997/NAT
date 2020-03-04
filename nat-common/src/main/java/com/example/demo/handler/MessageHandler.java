package com.example.demo.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import com.example.demo.protocol.Message;
import com.example.demo.protocol.MessageType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageHandler extends ChannelInboundHandlerAdapter {

    protected ChannelHandlerContext ctx;

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("捕获未知异常...");
        cause.printStackTrace();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) { // 一段时间内没有数据接收
                log.error("关闭通道...");
                ctx.close();
            } else if (e.state() == IdleState.WRITER_IDLE) { // 一段时间内没有数据发送
                log.info("心跳检测...");
                Message message = new Message();
                message.setType(MessageType.KEEPALIVE);
                ctx.writeAndFlush(message);
            }
        }
    }
}
