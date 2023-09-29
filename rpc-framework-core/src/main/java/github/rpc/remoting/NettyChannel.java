package github.rpc.remoting;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyChannel implements Channel{
    private io.netty.channel.Channel channel;
    public NettyChannel(io.netty.channel.Channel channel){
        this.channel = channel;
    }
    @Override
    public void send(Object message) {
        log.info("send channel {}",channel);
        channel.writeAndFlush(message);
    }

    public static Channel getOrAddNettyChannel(io.netty.channel.Channel channel){
        return new NettyChannel(channel);
    }
}
