package github.rpc.remoting.exchange;

import github.rpc.common.RpcRequest;
import github.rpc.common.URL;
import github.rpc.remoting.client.AbstractClient;
import github.rpc.serializer.Serializer;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.util.concurrent.CompletableFuture;

public class Http1ExchangeClient {
    private AbstractClient client;
    public Http1ExchangeClient(AbstractClient client){
        this.client = client;
    }

    public CompletableFuture<Object> request(RpcRequest request, URL url){
        DefaultFuture future = new DefaultFuture(request,url);
        int timeout = Integer.parseInt(url.getTimeout());
        String serializerType = url.getSerializerType();
        Serializer serializer = Serializer.getSerializerByType(Integer.parseInt(serializerType));
        byte[] bytes = serializer.serialize(request);
        DefaultFullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                request.getRpcServiceAndMethodName(), Unpooled.copiedBuffer(bytes));
        httpRequest.headers().set("path",request.getRpcServiceAndMethodName());
        httpRequest.headers().set("serializer",url.getSerializerType());
        httpRequest.headers().set("messageType",0);
        httpRequest.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        httpRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        if (timeout == 0 || timeout < 0){
            client.send(httpRequest); //异步发送，提交给rpc框架线程池
        }else{
            client.send(request,timeout); //异步发送，提交给rpc框架线程池
        }
        return future;
    }
}
