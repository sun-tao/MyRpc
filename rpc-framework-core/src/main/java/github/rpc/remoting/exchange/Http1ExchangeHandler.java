package github.rpc.remoting.exchange;

import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.remoting.Channel;
import github.rpc.remoting.ChannelHandler;
import github.rpc.remoting.HandlerDelegate;
import github.rpc.serializer.Serializer;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.concurrent.CompletableFuture;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Http1ExchangeHandler implements HandlerDelegate {
    private ExchangeChannelHandler handler; // 持有协议层传来的handler，是逻辑层面最顶端的handler，封装了reply语义
    public Http1ExchangeHandler(ExchangeChannelHandler handler){
        this.handler = handler;
    }
    @Override
    public void received(Channel channel, Object message) {
        if (message instanceof RpcRequest){ // 处理request请求
            RpcRequest request = (RpcRequest) message;
            handleRequest(channel,request);
        }else if (message instanceof RpcResponse){
            RpcResponse response = (RpcResponse) message;
            handleResponse(channel,response);
        }
    }

    private void handleRequest(Channel channel,RpcRequest request){
        RpcResponse response = new RpcResponse();
        CompletableFuture<Object> future = handler.reply(channel, request); //异步化
        future.whenComplete((result,exception)->{
            if (exception == null){
                response.setRequestId(request.getRequestId());
                response.setData(result);
                Serializer serializerByType = Serializer.getSerializerByType(0); //jdk序列化
                byte[] bytes = serializerByType.serialize(response);
                DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK,Unpooled.copiedBuffer(bytes));
                httpResponse.headers().set("serializer",0);
                httpResponse.headers().set("messageType",0);
                httpResponse.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
                httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
                channel.send(httpResponse);  // 回复client
            }else{
                throw new RuntimeException(exception);
            }
        });
    }

    private void handleResponse(Channel channel,RpcResponse response){
        DefaultFuture.received(response);
    }


    @Override
    public void sent(Channel channel, Object message) {

    }

    @Override
    public ChannelHandler getChannelHandler() {
        return null;
    }
}
