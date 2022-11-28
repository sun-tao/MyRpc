import github.rpc.common.SingletonFactory;
import github.rpc.config.SpringConfig;
import github.rpc.provider.ServiceProvider;
import github.rpc.server.NettyRpcServer;
import github.rpc.server.RpcServer;
import github.rpc.serviceImpl.BlogServiceImpl;
import github.rpc.serviceImpl.UserserviceImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Random;


public class TestServer {
    public static void main(String[] args) {
        ApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);
        NettyRpcServer rpcServer = SingletonFactory.getInstance(NettyRpcServer.class);
        rpcServer.start();
    }
}
