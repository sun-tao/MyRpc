package github.rpc.remoting;

public interface HandlerDelegate extends ChannelHandler{
    ChannelHandler getChannelHandler();
}
