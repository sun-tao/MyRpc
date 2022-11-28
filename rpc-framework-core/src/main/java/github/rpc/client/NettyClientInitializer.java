package github.rpc.client;

import github.rpc.serializer.Decode;
import github.rpc.serializer.Encode;
import github.rpc.serializer.ObjectSerializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new Decode());
        // 在此选择序列化方式，现在可以选择的方式有：java原生序列化方式 以及 基于Protubuf的高效序列化方式
        pipeline.addLast(new Encode(new ObjectSerializer()));

        // 客户端handler处理
        pipeline.addLast(new NettyRpcClientHandler());
    }
}
