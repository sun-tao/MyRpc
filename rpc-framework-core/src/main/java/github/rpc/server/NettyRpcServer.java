package github.rpc.server;

import github.rpc.provider.ServiceProvider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.stereotype.Component;

import java.util.Map;

// 使用Netty框架作RPC的网络通信方式
public class NettyRpcServer implements RpcServer {
    private ServiceProvider serviceProvider;
    public static final int port = 8100;
//    public NettyRpcServer(Map<String,Object> serviceProvider,int port){
//        this.serviceProvider = serviceProvider;
//        this.port = port;
//    }
    public NettyRpcServer(){

    }
    public void setServiceProvider(ServiceProvider serviceProvider){
        this.serviceProvider = serviceProvider;
    }
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer(serviceProvider.getServiceProvider()));

            // 上述为设置服务端Netty初始化代码
            // 下面启动服务器
            ChannelFuture f = b.bind(port).sync();

            // 死循环监听端口，阻塞在此，不会进入finally的逻辑，除非监听到关闭的事件
            f.channel().closeFuture().sync();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }

    public void stop() {

    }
}
