package github.rpc.remoting;

public interface ChannelHandler {
    void received(Channel channel,Object message);
    void sent(Channel channel,Object message);
}
