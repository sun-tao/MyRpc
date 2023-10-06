package github.rpc.remoting.server;

import github.rpc.common.URL;
import github.rpc.remoting.AbstractEndpoint;
import github.rpc.remoting.Channel;
import github.rpc.remoting.ChannelHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

public abstract class AbstractServer extends AbstractEndpoint {
    private InetSocketAddress bindAddress;

    public AbstractServer(URL url, ChannelHandler handler) {
        super(url, handler);
        bindAddress = getUrl().toInetSocketAddress();
        doOpen();
    }

    protected InetSocketAddress getBindAddress(){
        return bindAddress;
    }

    public abstract void doOpen();

    public abstract void doClose();



}
