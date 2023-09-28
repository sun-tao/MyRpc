package github.rpc.remoting.server;

import github.rpc.Invoker;
import github.rpc.common.URL;
import github.rpc.remoting.*;
import github.rpc.remoting.Channel;
import github.rpc.remoting.ChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

// 使用Netty框架作RPC的网络通信方式
@Slf4j
public class NettyServer extends AbstractServer {
    // todo : 多服务、多协议暴露
    private ServerBootstrap bootstrap;
    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyServer(URL url, ChannelHandler handler) {
        super(url, handler);
    }

    @Override
    public void doOpen() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        NettyServerHandler nettyServerHandler = new NettyServerHandler(getHandler());
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline()
                                        // fixme: 目前没有用到abstractEndpoint的能力，写死了Codec，要改进这块
                                        .addLast("decoder",new NettyCodecAdapter(getCodec()).getDecoder())
                                        .addLast("encoder",new NettyCodecAdapter(getCodec()).getEncoder())
                                        .addLast("handler",nettyServerHandler);
                            }
                        }
                );
        try {
            bootstrap.bind(getBindAddress()).sync(); // 开启监听端口
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doClose() {

    }
}
