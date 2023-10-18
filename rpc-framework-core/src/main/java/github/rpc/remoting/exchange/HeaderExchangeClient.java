package github.rpc.remoting.exchange;

import github.rpc.common.RpcRequest;
import github.rpc.common.URL;
import github.rpc.remoting.client.AbstractClient;
import github.rpc.serializer.Serializer;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.util.concurrent.CompletableFuture;

public class HeaderExchangeClient {
    private AbstractClient client;
    public HeaderExchangeClient(AbstractClient client){
        this.client = client;
    }

    public CompletableFuture<Object> request(RpcRequest request, URL url){
        DefaultFuture future = new DefaultFuture(request,url);
        int timeout = Integer.parseInt(url.getTimeout());
        if (timeout == 0 || timeout < 0){
            client.send(request); //异步发送，提交给rpc框架线程池
        }else{
            client.send(request,timeout); //异步发送，提交给rpc框架线程池
        }
        return future;
    }
}
