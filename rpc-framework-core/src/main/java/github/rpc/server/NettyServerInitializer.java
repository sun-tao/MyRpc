package github.rpc.server;


import github.rpc.serializer.Decode;
import github.rpc.serializer.Encode;
import github.rpc.serializer.ObjectSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.util.Map;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private Map<String,Object> serviceProvider;

    NettyServerInitializer(Map<String,Object> serviceProvider){
        this.serviceProvider = serviceProvider;
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast(new Decode());  // in1
        // 在此选择序列化方式，现在可以选择的方式有：java原生序列化方式 以及 基于Protubuf的高效序列化方式
        pipeline.addLast(new Encode(new ObjectSerializer()));  // out2
        // 服务端handler处理 : 读取request + 返回response
        pipeline.addLast(new NettyRpcServerHandler(serviceProvider)); // in2 out1

    }
}
