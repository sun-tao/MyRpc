import github.rpc.BlogService;
import github.rpc.Userservice;
import github.rpc.common.URL;
import github.rpc.config.CustomShutdownHook;
import github.rpc.config.SpringConfig;
import github.rpc.extension.ExtensionLoader;

import github.rpc.remoting.server.RpcServerProxy;
import github.rpc.serviceImpl.BlogServiceImpl;
import github.rpc.serviceImpl.UserserviceImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Random;


public class DemoProvider {
    public static void main(String[] args) {
        URL url1 = new URL();
        url1.setIp("127.0.0.1");
        url1.setPort("20880");
        URL url2 = new URL();
        url2.setIp("127.0.0.1");
        url2.setPort("20880");
        String serviceName1 = BlogService.class.getName();
        url1.setServiceName(serviceName1);
        String serviceName2 = Userservice.class.getName();
        url2.setServiceName(serviceName2);
        RpcServerProxy rpcServerProxy = new RpcServerProxy();
        rpcServerProxy.setRef(serviceName1,new BlogServiceImpl());
        rpcServerProxy.setUrl(serviceName1,url1);
        rpcServerProxy.setRef(serviceName2,new UserserviceImpl());
        rpcServerProxy.setUrl(serviceName2,url2);
        rpcServerProxy.exportAndRegister();
        rpcServerProxy.await();
    }
}
