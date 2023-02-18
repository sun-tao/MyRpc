package github.rpc.client;

import github.rpc.serializer.Decode;
import github.rpc.serializer.Encode;
import github.rpc.serializer.HessianSerializer;
import github.rpc.serializer.ObjectSerializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 每15s 客户端向服务端发送一次心跳包
        pipeline.addLast(new IdleStateHandler(0,15,0, TimeUnit.SECONDS));
        pipeline.addLast(new Decode());
        // 在此选择序列化方式，现在可以选择的方式有：java原生序列化方式 以及 基于Hessian的高效序列化方式
        pipeline.addLast(new Encode(new HessianSerializer()));
        // 客户端handler处理
        pipeline.addLast(new NettyRpcClientHandler());
    }
}
