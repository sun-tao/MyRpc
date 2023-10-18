package github.rpc.remoting.client;


import github.rpc.common.*;
import github.rpc.extension.ExtensionLoader;
import github.rpc.remoting.*;
import github.rpc.remoting.ChannelHandler;
import github.rpc.remoting.exchange.DefaultFuture;
import github.rpc.remoting.server.NettyServerHandler;
import github.rpc.util.InternalTimer;
import github.rpc.util.MyRpcTimer;
import github.rpc.util.TimerTask;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.*;

@Slf4j
public class Http1Client extends AbstractClient {
    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;
    private Channel channel;
    private MyRpcTimer timer = (MyRpcTimer) ExtensionLoader.getExtensionLoader(InternalTimer.class).getExtension("timer");

    public Http1Client(URL url, ChannelHandler handler) {
        super(url, handler);
    }

    @Override
    public void doOpen() {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("decoder", new HttpResponseDecoder());
                        pipeline.addLast("encoder",new HttpRequestEncoder());
                        pipeline.addLast("handler",new Http1ClientHandler(getHandler()));
                    }
                })
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
    }

    @Override
    public void doConnect() {
        URL url = getUrl();
        try {
            channel = bootstrap.connect(url.toInetSocketAddress()).sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.warn("connected channel {}",channel);
    }

    @Override
    public void send(Object message) { // 网络层面真正的发送
        log.info("client send {}",channel);
        channel.writeAndFlush(message);
    }

    @Override
    public void send(Object message, int timeout) {
        channel.writeAndFlush(message);
        TimerTask timerTask = new TimerTask(timeout) {
            @Override
            public void run() { // 到期之后从defaultfuture中删掉这个future，避免oom，同时完成future，让这个future抛异常，解除上层的阻塞
                RpcRequest request = (RpcRequest) message;
                log.warn("request {} timeout",request);
                DefaultFuture.cancel(message);
            }
        };
        DefaultFuture.registerTimerTask((RpcRequest) message,timerTask);
        timer.add(timerTask);
    }


    private github.rpc.remoting.Channel wrapChannel(Channel channel){
        return new NettyChannel(channel);
    }

}
