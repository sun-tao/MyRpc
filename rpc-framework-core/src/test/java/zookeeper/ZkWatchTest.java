package zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class ZkWatchTest {
    public static void main(String[] args) throws Exception {
        ZkWatch zkWatch = new ZkWatch();
        zkWatch.connection(); // 连接并新建测试结点
        zkWatch.nodeCache(); // 注册监听 Test结点
        zkWatch.register("t1");
        Thread.sleep(1000);
        // Test结点下新建结点
        zkWatch.register("t2");
        Thread.sleep(1000);
        // Test结点下删除结点
        zkWatch.deleteNode("t1");
    }
}
