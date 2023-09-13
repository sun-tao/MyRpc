package github.rpc.server;

import github.rpc.Invoker;
import github.rpc.common.URL;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

// 使用Netty框架作RPC的网络通信方式
@Slf4j
public class NettyRpcServer implements RpcServer {
    // todo : 多服务、多协议暴露
    public volatile int state = 0;  // 单服务、单端口启动状态 0-关闭 1-启动
    public Map<String, Invoker> exportMap = new HashMap<>();
    public NettyRpcServer(){
    }
    public void export(String key,Invoker invoker){
        URL url = invoker.getURL();
        int port = Integer.parseInt(url.getPort());
        start(port); // local export
        exportMap.put(key,invoker);
    }
    public void start(int port) { // bind
        if (state == 1){
            return;
        }
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 开启TCP心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childHandler(new NettyServerInitializer(exportMap));

            // 上述为设置服务端Netty初始化代码
            // 下面启动服务器
            log.info("Server 开启成功 端口:{}",port);
            ChannelFuture f = b.bind(port).sync();
            // 死循环监听端口，阻塞在此，不会进入finally的逻辑，除非监听到关闭的事件
            state = 1;
//            f.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stop() {
        if (state == 0){
            log.warn("NettyRpcServer.stop error");
            return;
        }
        state = 0;
    }
}
