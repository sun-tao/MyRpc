package github.rpc.server;


import github.rpc.serializer.Decode;
import github.rpc.serializer.Encode;
import github.rpc.serializer.ObjectSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private Map<String,Object> serviceProvider;

    NettyServerInitializer(Map<String,Object> serviceProvider){
        this.serviceProvider = serviceProvider;
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // 服务端30s未收到消息则判定当前连接的客户端下线，关闭连接
        pipeline.addLast(new IdleStateHandler(5,0,0, TimeUnit.SECONDS));
        pipeline.addLast(new Decode());  // in1
        // 在此选择序列化方式，现在可以选择的方式有：java原生序列化方式 以及 基于Protubuf的高效序列化方式
        pipeline.addLast(new Encode(new ObjectSerializer()));  // out2
        // 服务端handler处理 : 读取request + 返回response
        pipeline.addLast(new NettyRpcServerHandler(serviceProvider)); // in2 out1

    }
}
