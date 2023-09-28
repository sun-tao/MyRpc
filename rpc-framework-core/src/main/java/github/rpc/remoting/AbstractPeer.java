package github.rpc.remoting;

import github.rpc.common.URL;

public class AbstractPeer implements EndPoint,ChannelHandler{
    private URL url;
    private ChannelHandler handler;
    public AbstractPeer(URL url,ChannelHandler handler){
        this.url = url;
        this.handler = handler;
    }
    protected URL getUrl(){
        return url;
    }
    protected ChannelHandler getHandler(){
        return handler;
    }
    @Override
    public void received(Channel channel, Object message) {

    }

    @Override
    public void sent(Channel channel, Object message) {

    }

    @Override
    public void send(Channel channel, Object message) {

    }
}
