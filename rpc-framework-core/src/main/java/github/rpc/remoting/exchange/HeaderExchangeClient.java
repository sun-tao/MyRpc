package github.rpc.remoting.exchange;

import github.rpc.common.RpcRequest;
import github.rpc.common.URL;
import github.rpc.remoting.client.AbstractClient;

import java.util.concurrent.CompletableFuture;

public class HeaderExchangeClient {
    private AbstractClient client;
    public HeaderExchangeClient(AbstractClient client){
        this.client = client;
    }

    // todo:超时时间
    public CompletableFuture<Object> request(Object request,int timeout){
        return null;
    }

    public CompletableFuture<Object> request(RpcRequest request, URL url){
        DefaultFuture future = new DefaultFuture(request,url);
        client.send(request); //异步发送，提交给rpc框架线程池
        return future;
    }
}
