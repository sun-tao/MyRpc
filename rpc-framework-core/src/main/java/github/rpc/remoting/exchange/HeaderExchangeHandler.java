package github.rpc.remoting.exchange;

import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.remoting.Channel;
import github.rpc.remoting.ChannelHandler;
import github.rpc.remoting.HandlerDelegate;
import github.rpc.remoting.NettyChannel;
import github.rpc.support.RpcExceptionAdapter;
import lombok.extern.slf4j.Slf4j;
import sun.nio.ch.Net;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
@Slf4j
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
                //针对异常情况返回异常，使得限流之后能够正常返回
                response.setRequestId(request.getRequestId());
                Exception e = (Exception) exception;
                response.setException(RpcExceptionAdapter.adapter(e));
                log.warn("handle request exception:{}",exception.toString());
                channel.send(response);
            }
        });
    }

    private void handleResponse(Channel channel,RpcResponse response){
        DefaultFuture.received(response);
    }


}
