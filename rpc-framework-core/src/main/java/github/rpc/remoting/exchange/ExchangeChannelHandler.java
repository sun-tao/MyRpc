package github.rpc.remoting.exchange;

import github.rpc.remoting.Channel;
import github.rpc.remoting.ChannelHandler;

import java.util.concurrent.CompletableFuture;

public interface ExchangeChannelHandler extends ChannelHandler {
    CompletableFuture<Object> reply(Channel channel, Object request);
}
