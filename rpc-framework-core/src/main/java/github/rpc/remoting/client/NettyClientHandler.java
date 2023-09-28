package github.rpc.remoting.client;

import github.rpc.remoting.Channel;
import github.rpc.remoting.ChannelHandler;
import github.rpc.remoting.NettyChannel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
@io.netty.channel.ChannelHandler.Sharable
public class NettyClientHandler extends ChannelDuplexHandler {
    private ChannelHandler handler;
    NettyClientHandler(ChannelHandler handler){
        this.handler = handler;
    }
    // todo:客户端的发送接收链路
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
}
