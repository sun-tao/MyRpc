package github.rpc.registry.zk;

import github.rpc.common.RpcRequest;
import github.rpc.loadbalance.LoadBalance;
import github.rpc.registry.ServiceRegister;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// 与zookeeper交互的类
@Slf4j
public class ZkServiceRegister implements ServiceRegister {
    private CuratorFramework client;
    // zookeeper根节点路径
    private static final String ROOT_PATH = "MyRpc";
    public ZkServiceRegister(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // 连接zookeeper服务器
        this.client  =
                CuratorFrameworkFactory.builder().connectString("localhost:2181").retryPolicy(retryPolicy).sessionTimeoutMs(40000).namespace(ROOT_PATH).build();
        this.client.start();
        log.info("zookeeper连接成功！");
    }

    public void quit(){
        CloseableUtils.closeQuietly(client);
    }
    // server
    public void register(String serviceName, String host, String port, Map<String,String> parameters) {
        // RPC服务端使用
        try {
            // 如果当前的zookeeper服务器上，没注册过这个服务
            if (client.checkExists().forPath("/" + serviceName + "/provider") == null){
                // 永久注册该服务名,因为可能有其他的rpc服务器也在zookeeper上注册了该服务名，因此服务名不能下线
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + serviceName + "/provider");
            }
            // 临时性注册
            String path;
            if (parameters.isEmpty()){
                path = "/" + serviceName + "/provider" + "/" + getServiceAddress(host,port);
            }else {
                String weight = parameters.get("weight");
                path = "/" + serviceName + "/provider" + "/" + getServiceAddress(host,port) + "?weight=" + weight;
            }
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                shutdown(path);
            }));
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shutdown(String path){
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e) {
            log.error("ZkServiceRegister shutdownErr");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerRoute(String serviceName) {
        try {
            if (client.checkExists().forPath("/" + serviceName + "/route") == null){
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + serviceName + "/route");
            }
            List<String> routes = getRoutes(serviceName);
            for (int i = 0 ; i < routes.size() ; i++){
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/" + serviceName + "/route" + "/" + routes.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getRoutes(String serviceName) throws IOException {
        String path = ZkServiceRegister.class.getResource("/").getPath();
        String filename = path + "/" + serviceName + "/iproute";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        String line = null;
        List<String> result = new ArrayList<>();
        while((line = reader.readLine()) != null){
            result.add(line);
        }
        return result;
    }
    private String getServiceAddress(String host,String port){
        return host + ":" + port;
    }

    private InetSocketAddress parseAddress(String address){
        String[] strs = address.split(":");
        return new InetSocketAddress(strs[0],Integer.parseInt(strs[1]));
    }

}
