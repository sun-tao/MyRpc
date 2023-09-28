package github.rpc.remoting.server;


import github.rpc.Invoker;
import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.remoting.Channel;
import github.rpc.remoting.ChannelHandler;
import github.rpc.remoting.NettyChannel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@io.netty.channel.ChannelHandler.Sharable
public class NettyServerHandler extends ChannelDuplexHandler {
    private ChannelHandler handler;
    NettyServerHandler(ChannelHandler handler){
        this.handler = handler;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 对于服务端来说，这里直接就是Request请求体
        Channel channel = NettyChannel.getOrAddNettyChannel(ctx.channel());
        handler.received(channel,msg);
    }
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise){
        try {
            super.write(ctx,msg,promise);
            Channel channel = NettyChannel.getOrAddNettyChannel(ctx.channel());
            handler.sent(channel,msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private RpcResponse getResponse(RpcRequest rpcRequest,boolean heartBeat){
        if (heartBeat == true){
            RpcResponse pong = RpcResponse.wrapperResult("PONG", rpcRequest.getRequestId());
            pong.setMessageType(1);
            return pong;
        }
        return null;
    }

    // 如果30s没有请求，则断开当前连接
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        }
        super.userEventTriggered(ctx,evt);
    }
}
