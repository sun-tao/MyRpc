//package github.rpc.remoting.server;
//
//
//import github.rpc.Invoker;
//import github.rpc.serializer.Decode;
//import github.rpc.serializer.Encode;
//import github.rpc.serializer.HessianSerializer;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.ChannelPipeline;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.handler.timeout.IdleStateHandler;
//
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
//    private Map<String, Invoker> exportedMap;
//
//    NettyServerInitializer(Map<String,Invoker> exportedMap){
//        this.exportedMap = exportedMap;
//    }
//
//    protected void initChannel(SocketChannel socketChannel) throws Exception {
//        ChannelPipeline pipeline = socketChannel.pipeline();
//        // 服务端30s未收到消息则判定当前连接的客户端下线，关闭连接
//        pipeline.addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));
//        pipeline.addLast(new Decode());  // in1
//        // 在此选择序列化方式，现在可以选择的方式有：1.java原生序列化方式 2.
//        pipeline.addLast(new Encode(new HessianSerializer()));  // out2
//
//    }
//}
