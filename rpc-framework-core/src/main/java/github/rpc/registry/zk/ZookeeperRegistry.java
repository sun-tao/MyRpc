package github.rpc.registry.zk;

import github.rpc.Invoker;
import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.URL;
import github.rpc.exporter.Exporter;
import github.rpc.registry.AbstractRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {
    private CuratorFramework client;
    // zookeeper根节点路径
    private static final String ROOT_PATH = "MyRpc";
    public ZookeeperRegistry(URL url) {
        super(url);
        String registryInstance = url.parseRegistryInstance();
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.client  = CuratorFrameworkFactory.builder().connectString(registryInstance).retryPolicy(retryPolicy).sessionTimeoutMs(40000).namespace(ROOT_PATH).build();
        this.client.start();
        log.info("zookeeper连接成功！");
    }
    @Override
    public void register(URL url) {
        String serviceName = url.getServiceName();
        String servicePath = "/"  + serviceName +"/provider";
        // RPC服务端使用
        try {
            // 如果当前的zookeeper服务器上，没注册过这个服务
            if (client.checkExists().forPath(servicePath) == null){
                // 永久注册该服务名,因为可能有其他的rpc服务器也在zookeeper上注册了该服务名，因此服务名不能下线
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(servicePath);
            }
            // 临时性注册 本rpc服务器的IP+host?weight=xxx if have weight
            String nodeValue = url.parseUrl();
            String path = servicePath + "/" + nodeValue;
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
            log.info("register service {}" , nodeValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void subscribe(URL url) { // 注册中心层面的url，读取zk上所有服务的urls，写到内存中
        List<String> services = null;
        try {
            String serviceName = url.getServiceName();
            String servicePath = "/" + serviceName + "/provider"; // 单服务维度
            services = client.getChildren().forPath(servicePath);
            for (int i = 0 ; i < services.size() ; i++){ // 单个实例维度
                String serviceInstance = services.get(i);
                List<URL> urls = invokersUrl.computeIfAbsent(serviceName, k -> new ArrayList<>());
                URL url1 = URL.parseString2Url(serviceInstance);
                URL finUrl = mergeUrl(url,url1);
                urls.add(finUrl);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<Object> invoke(RpcRequest rpcRequest, URL url) {
        CompletableFuture<Object> future = cluster.invoke(rpcRequest,url);
        return future;
    }

    // fixme:临时性过渡写法，后续服务暴露的时候将全量url写入zk就不需要这个了
    private URL mergeUrl(URL consumerUrl,URL providerUrl){
        URL finUrl = new URL();
        finUrl.setProtocol(consumerUrl.getProtocol());
        finUrl.setIp(providerUrl.getIp());
        finUrl.setPort(providerUrl.getPort());
        finUrl.setServiceName(consumerUrl.getServiceName());
        finUrl.setApplicationName(consumerUrl.getApplicationName());
        finUrl.setLoadbalacne(consumerUrl.getLoadbalacne());
        finUrl.setClusterType(consumerUrl.getClusterType());
        finUrl.setConsumerAsync(consumerUrl.getConsumerAsync());
        finUrl.setTimeout(consumerUrl.getTimeout());
        return finUrl;
    }
}
