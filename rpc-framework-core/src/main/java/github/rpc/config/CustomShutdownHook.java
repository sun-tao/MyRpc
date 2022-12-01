package github.rpc.config;

import com.google.common.net.InetAddresses;
import github.rpc.common.SingletonFactory;
import github.rpc.registry.zk.ZkServiceRegister;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll(){
        log.info("addShutdownHook for clearAll");

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                ZkServiceRegister zkServiceRegister = SingletonFactory.getInstance(ZkServiceRegister.class);
                // 断开zookeeper客户端与服务器的连接，自动销毁上次连接创建的临时结点
                zkServiceRegister.quit();
                log.info("zookeeper成功断开连接");
                 }
            catch (Exception e){
                e.printStackTrace();
            }
        }));
    }
}
