package zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class ZkWatch {
    private CuratorFramework client;
    private static final String ROOT_PATH = "MyRpc";
    public void connection() throws Exception {
        // 连接zookeeper服务端
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.client  = CuratorFrameworkFactory.builder().connectString("localhost:2181").retryPolicy(retryPolicy).sessionTimeoutMs(40000).namespace(ROOT_PATH).build();
        this.client.start();
//        this.client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/Test");
    }

    public void nodeCache() throws Exception{
        //获取监听对象
        PathChildrenCache nodeCache = new PathChildrenCache(client, "/Test",true);
        //调用start方法开始监听 ，设置启动模式为同步加载节点数据
        //添加监听
        nodeCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        nodeCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
//                PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
//                System.out.println("节点数据变化,类型:" + pathChildrenCacheEvent.getType() + ",路径:" + pathChildrenCacheEvent.getData().getPath());
                if (pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED){
                    System.out.println("节点增加,类型:" + pathChildrenCacheEvent.getType() + ",路径:" + pathChildrenCacheEvent.getData().getPath());
                }else if (pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED){
                    System.out.println("节点删除,类型:" + pathChildrenCacheEvent.getType() + ",路径:" + pathChildrenCacheEvent.getData().getPath());
                }
            }
        });
    }

    public void deleteNode(String path) throws Exception {
        path = "/Test/" + path;
        client.delete().forPath(path);
    }

    public void register(String path) throws Exception {
        path = "/Test/" + path;
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
    }
}
