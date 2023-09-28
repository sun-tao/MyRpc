package github.rpc.remoting;

public class NettyChannel implements Channel{
    private io.netty.channel.Channel channel;
    public NettyChannel(io.netty.channel.Channel channel){
        this.channel = channel;
    }
    @Override
    public void send(Object message) {
        channel.writeAndFlush(message);
    }

    public static Channel getOrAddNettyChannel(io.netty.channel.Channel channel){
        return new NettyChannel(channel);
    }
}
