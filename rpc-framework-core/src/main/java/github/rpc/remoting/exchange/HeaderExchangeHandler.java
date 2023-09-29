package github.rpc.remoting.exchange;

import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.remoting.Channel;
import github.rpc.remoting.ChannelHandler;
import github.rpc.remoting.HandlerDelegate;
import github.rpc.remoting.NettyChannel;
import sun.nio.ch.Net;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class HeaderExchangeHandler implements HandlerDelegate {
    private ExchangeChannelHandler handler; // 持有协议层传来的handler，是逻辑层面最顶端的handler，封装了reply语义
    public HeaderExchangeHandler(ExchangeChannelHandler handler){
        this.handler = handler;
    }
    @Override
    public void received(Channel channel, Object message) { // request和response语义
        if (message instanceof RpcRequest){ // 处理request请求
            RpcRequest request = (RpcRequest) message;
            handleRequest(channel,request);
        }else if (message instanceof RpcResponse){
            RpcResponse response = (RpcResponse) message;
            handleResponse(channel,response);
        }
    }

    @Override
    public void sent(Channel channel, Object message) {

    }

    @Override
    public ChannelHandler getChannelHandler() {
        return handler;
    }

    private void handleRequest(Channel channel,RpcRequest request){
        RpcResponse response = new RpcResponse();
        CompletableFuture<Object> future = handler.reply(channel, request); //异步化
        future.whenComplete((result,exception)->{
            if (exception == null){
                response.setRequestId(request.getRequestId());
                response.setData(result);
                channel.send(response);  // 回复client
            }else{
                throw new RuntimeException(exception);
            }
        });
    }

    private void handleResponse(Channel channel,RpcResponse response){
        DefaultFuture.received(response);
    }


}