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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

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
//        System.out.println(client.getChildren());
        CloseableUtils.closeQuietly(client);
    }
    public void register(String serviceName, InetSocketAddress serverAddress) {
        // RPC服务端使用
        try {
            // 如果当前的zookeeper服务器上，没注册过这个服务
            if (client.checkExists().forPath("/" + serviceName) == null){
                // 永久注册该服务名,因为可能有其他的rpc服务器也在zookeeper上注册了该服务名，因此服务名不能下线
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + serviceName);
            }
            // 临时性注册 本rpc服务器的IP+host
            String path = "/" + serviceName + "/" + getServiceAddress(serverAddress);
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InetSocketAddress serviceDiscovery(String serviceName, LoadBalance loadBalance, RpcRequest rpcRequest,List<String> invokers,List<String> invoked) {
        // RPC客户端使用
        try {
            if (invokers == null){
                invokers = client.getChildren().forPath("/"+serviceName);
            }
            if (invokers == null || invokers.size() == 0){
                log.info("无可用的服务!");
                return null;
            }
            log.info("zk上的服务器列表有{}" , invokers);
            // 排除invoked中已经包含了的
            List<String> result = new ArrayList<>();
            for (int i = 0 ; i < invokers.size() ; i++){
                if (invoked != null && invoked.contains(invokers.get(i))){
                    continue;
                }
                result.add(invokers.get(i));
            }
            if (result == null || result.size() == 0){
                log.info("无可用的服务!");
                return null;
            }
            String address = loadBalance.loadBalance(result,rpcRequest);
            return parseAddress(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getServiceAddress(InetSocketAddress serverAddress){
        return serverAddress.getHostName() + ":" + serverAddress.getPort();
    }

    private InetSocketAddress parseAddress(String address){
        String[] strs = address.split(":");
        return new InetSocketAddress(strs[0],Integer.parseInt(strs[1]));
    }

    public List<String> getInvokers(String serviceName) {
        try {
            List<String> list = client.getChildren().forPath("/"+serviceName);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
