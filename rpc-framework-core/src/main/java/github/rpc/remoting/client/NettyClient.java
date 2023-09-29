package github.rpc.remoting.client;


import github.rpc.common.*;
import github.rpc.remoting.*;
import github.rpc.remoting.ChannelHandler;
import github.rpc.remoting.server.NettyServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.*;

@Slf4j
public class NettyClient extends AbstractClient {
    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;
    private Channel channel;

    public NettyClient(URL url, ChannelHandler handler) {
        super(url, handler);
    }

    @Override
    public void doOpen() {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        NettyClientHandler nettyClientHandler = new NettyClientHandler(getHandler());
        NettyCodecAdapter nettyCodecAdapter = new NettyCodecAdapter(getCodec());
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("decoder", nettyCodecAdapter.getDecoder());
                        pipeline.addLast("encoder",nettyCodecAdapter.getEncoder());
                        pipeline.addLast("handler",nettyClientHandler);
                    }
                })
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
    }

    @Override
    public void doConnect() {
        URL url = getUrl();
        ChannelFuture connect = bootstrap.connect(url.toInetSocketAddress());
        channel = connect.channel();
        log.warn("connected channel {}",channel);
    }

    @Override
    public void send(Object message) { // 网络层面真正的发送
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                channel.writeAndFlush(message);
            }
        });
    }

    private github.rpc.remoting.Channel wrapChannel(Channel channel){
        return new NettyChannel(channel);
    }

}
