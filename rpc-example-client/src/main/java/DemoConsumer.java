import github.rpc.*;
import github.rpc.remoting.MyRpcDecoder;
import github.rpc.remoting.MyRpcEncoder;
import github.rpc.remoting.client.RpcClientProxy;
import github.rpc.common.URL;
import github.rpc.config.SpringConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class DemoConsumer {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        RpcClientProxy proxy = new RpcClientProxy();
        URL url1 = new URL();
        url1.setServiceName(BlogService.class.getName());
        url1.setLoadBalacne("consistentHash");
        proxy.setUrl(BlogService.class.getName(),url1);
        URL url2 = new URL();
        url2.setServiceName(Userservice.class.getName());
        url2.setConsumer_async("true");
        url2.setLoadBalacne("consistentHash");
        proxy.setUrl(Userservice.class.getName(),url2);
        proxy.refer();

        BlogService blogService = (BlogService) proxy.getProxy(BlogService.class);
        Userservice userservice = (Userservice) proxy.getProxy(Userservice.class);
        System.out.println(blogService.getBlogByid(5));
        CompletableFuture<String> myrpc = userservice.sayHelloAsync("myrpc");
        myrpc.whenComplete((r,t)->{
            System.out.println(r);
        });
        System.out.println("---");
        Thread.sleep(1000);
    }
}
