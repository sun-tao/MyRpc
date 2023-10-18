package github.rpc.remoting.server;

import github.rpc.common.URL;
import github.rpc.remoting.Channel;
import github.rpc.remoting.ChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class Http1Server extends AbstractServer {
    private ServerBootstrap bootstrap;
    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    public Http1Server(URL url, ChannelHandler handler) {
        super(url, handler);
    }

    @Override
    public void doOpen() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline()
                                        .addLast("decoder",new HttpRequestDecoder())
                                        .addLast("encoder",new HttpResponseEncoder())
                                        .addLast("handler",new Http1ServerHandler(getHandler()));
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
