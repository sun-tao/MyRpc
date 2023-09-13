package github.rpc.protocol;

import github.rpc.Invoker;
import github.rpc.client.NettyRpcClient;
import github.rpc.common.URL;
import github.rpc.exporter.Exporter;
import github.rpc.exporter.MyRpcExporter;
import github.rpc.server.NettyRpcServer;

import java.util.HashMap;
import java.util.Map;

public class MyRpcProtocol implements Protocol{
    public NettyRpcServer nettyRpcServer = new NettyRpcServer();   //单例的
    public Map<String,Exporter> exportedMap = new HashMap<>();
    @Override
    public Exporter export(Invoker invoker) {
        URL url = invoker.getURL();
        String key = url.getServiceName();
        nettyRpcServer.export(key,invoker);
        Exporter exporter = new MyRpcExporter(key,invoker);
        exportedMap.put(key,exporter);
        return exporter;
    }
    @Override
    public Invoker refer(URL url) {
        MyRpcInvoker invoker = new MyRpcInvoker();
        invoker.initInvoker(url);
        return invoker;
    }
}
