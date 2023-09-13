package github.rpc.registry;

import github.rpc.Invoker;
import github.rpc.annotation.Spi;
import github.rpc.cluster.Cluster;
import github.rpc.cluster.FailoverCluster;
import github.rpc.common.URL;
import github.rpc.exporter.Exporter;
import github.rpc.extension.ExtensionLoader;
import github.rpc.protocol.Protocol;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Spi
public abstract class AbstractRegistry implements Registry {
    public ConcurrentHashMap<String, List<URL>> invokersUrl = new ConcurrentHashMap<>();
    public Cluster cluster;
    public AbstractRegistry(URL url){
        String clusterType = url.getClusterType();
        cluster = ExtensionLoader.getExtensionLoader(Cluster.class).getExtension(clusterType);
    }
    @Override
    public Cluster refer(URL url){    // 消费端,注册中心层面的url，ip和port需要是注册中心，之后convert成提供方url
        subscribe(url);  // 去注册中心读取url到invokersUrl中
        for (Map.Entry<String,List<URL>> entry : invokersUrl.entrySet()){ // 服务维度
            List<URL> urls = entry.getValue();
            cluster.refer(urls);
        }
        return cluster;
    }
    @Override
    public Exporter export(Invoker invoker){
        // do export
        URL url = invoker.getURL();
        String protocol = url.getProtocol();
        Protocol ptl = ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(protocol);
        Exporter export = ptl.export(invoker);
        return export;
    }
}
