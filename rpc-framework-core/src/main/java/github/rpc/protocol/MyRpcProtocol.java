package github.rpc.protocol;

import github.rpc.Invoker;
import github.rpc.common.RpcRequest;
import github.rpc.remoting.Channel;
import github.rpc.remoting.ChannelHandler;
import github.rpc.common.URL;
import github.rpc.exporter.Exporter;
import github.rpc.exporter.MyRpcExporter;
import github.rpc.remoting.exchange.ExchangeChannelHandler;
import github.rpc.remoting.exchange.HeaderExchangeHandler;
import github.rpc.remoting.server.AbstractServer;
import github.rpc.remoting.server.NettyServer;
import github.rpc.remoting.transport.AllDispatcherHandler;
import github.rpc.remoting.transport.DecodeHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
@Slf4j
public class MyRpcProtocol implements Protocol{
    private Map<String, AbstractServer> serverMap = new HashMap<>();

    public class MyRpcExchangeChannelHandler implements ExchangeChannelHandler {

        @Override
        public void received(Channel channel, Object message) {
            log.info("MyRpcExchangeChannelHandler.received method call");
        }

        @Override
        public void sent(Channel channel, Object message) {
            log.info("MyRpcExchangeChannelHandler.sent method call");
        }

        @Override
        public CompletableFuture<Object> reply(Channel channel, Object message) {
            // 在此做方法的本地调用
            RpcRequest request = (RpcRequest) message;
            String service_name = ((RpcRequest) request).getInterfaceName();
            Exporter exporter = exportedMap.get(service_name);
            if (exporter == null){
                log.error("MyRpcProtocol.MyRpcExchangeChannelHandler.reply:no {} exporter",service_name);
                CompletableFuture<Object> future = new CompletableFuture<>();
                future.complete(null);
                return future;
            }
            Invoker invoker = exporter.getInvoker();
            CompletableFuture<Object> future = invoker.doInvoke(request);
            return future;
        }
    }

    public ExchangeChannelHandler handler = new MyRpcExchangeChannelHandler();
    public Map<String,Exporter> exportedMap = new HashMap<>();
    @Override
    public Exporter export(Invoker invoker) {
        URL url = invoker.getURL();
        String key = url.getServiceName();
        Exporter exporter = new MyRpcExporter(key,invoker);
        exportedMap.put(key,exporter);
        String instance = url.parseInstance(); // ip + port 连接层的链路和端口开放对各个服务只需要初始化一次即可
        if (serverMap.get(instance) == null){
            AbstractServer server = createServer(url, new AllDispatcherHandler(new DecodeHandler(new HeaderExchangeHandler(handler))));
            serverMap.put(instance,server);
        }
        return exporter;
    }
    public AbstractServer createServer(URL url,ChannelHandler handler){
        return new NettyServer(url,handler);
    }
    @Override
    public Invoker refer(URL url) {
        MyRpcInvoker invoker = new MyRpcInvoker(url,handler);
        return invoker;
    }
}
