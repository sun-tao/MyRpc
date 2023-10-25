package github.rpc.protocol;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import github.rpc.Invoker;
import github.rpc.common.RpcRequest;
import github.rpc.remoting.Channel;
import github.rpc.remoting.ChannelHandler;
import github.rpc.common.URL;
import github.rpc.exporter.Exporter;
import github.rpc.exporter.MyRpcExporter;
import github.rpc.remoting.exchange.ExchangeChannelHandler;
import github.rpc.remoting.exchange.HeaderExchangeHandler;
import github.rpc.remoting.exchange.Http1ExchangeHandler;
import github.rpc.remoting.server.AbstractServer;
import github.rpc.remoting.server.Http1Server;
import github.rpc.remoting.transport.AllDispatcherHandler;
import github.rpc.remoting.transport.DecodeHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
@Slf4j
public class Http1Protocol implements Protocol{
    private Map<String, AbstractServer> serverMap = new HashMap<>();

    public class Http1ExchangeChannelHandler implements ExchangeChannelHandler {

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
            String service_name = request.getInterfaceName();
            Exporter exporter = exportedMap.get(service_name);
            if (exporter == null){
                log.error("MyRpcProtocol.MyRpcExchangeChannelHandler.reply:no {} exporter",service_name);
                CompletableFuture<Object> future = new CompletableFuture<>();
                future.complete(null);
                return future;
            }

            Invoker invoker = exporter.getInvoker();
            try(Entry entry = SphU.entry(service_name)) {
                CompletableFuture<Object> future = invoker.doInvoke(request);
                return future;
            }catch (BlockException e){
//                log.warn(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    public ExchangeChannelHandler handler = new Http1ExchangeChannelHandler();
    public Map<String,Exporter> exportedMap = new HashMap<>();
    @Override
    public Exporter export(Invoker invoker) {
        URL url = invoker.getURL();
        String key = url.getServiceName();
        Exporter exporter = new MyRpcExporter(key,invoker);
        exportedMap.put(key,exporter);
        String instance = url.parseInstance(); // ip + port 连接层的链路和端口开放对各个服务只需要初始化一次即可
        if (serverMap.get(instance) == null){
            AbstractServer server = createServer(url, new AllDispatcherHandler(new DecodeHandler(new Http1ExchangeHandler(handler))));
            serverMap.put(instance,server);
        }
        return exporter;
    }
    public AbstractServer createServer(URL url,ChannelHandler handler){
        return new Http1Server(url,handler);
    }
    @Override
    public Invoker refer(URL url) {
        Http1Invoker invoker = new Http1Invoker(url,handler);
        return invoker;
    }
}
