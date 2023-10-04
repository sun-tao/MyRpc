package github.rpc.remoting.transport;

import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.remoting.Channel;
import github.rpc.remoting.ChannelHandler;
import github.rpc.remoting.HandlerDelegate;

public class DecodeHandler implements HandlerDelegate {
    private ChannelHandler handler;  // HeaderExchangeHandler对象
    public DecodeHandler(ChannelHandler handler){
        this.handler = handler;
    }
    @Override
    public ChannelHandler getChannelHandler() {
        return handler;
    }

    @Override
    public void received(Channel channel, Object message) {
        // 这边接收到的是由netty的decoder层解码完成的RpcRequest对象
        if (message instanceof RpcRequest){
            handler.received(channel,(RpcRequest) message);
        }else if(message instanceof RpcResponse) {
            handler.received(channel, (RpcResponse) message);
        }
    }

    @Override
    public void sent(Channel channel, Object message) {
        if (message instanceof RpcRequest){
            handler.sent(channel,(RpcRequest) message);
        }else  if (message instanceof RpcResponse){
            handler.sent(channel,(RpcResponse) message);
        }
    }
}
