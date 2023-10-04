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
        url1.setLoadbalacne("consistentHash");
        proxy.setUrl(BlogService.class.getName(),url1);
        URL url2 = new URL();
        url2.setServiceName(Userservice.class.getName());
        url2.setConsumerAsync("true");
        url2.setLoadbalacne("consistentHash");
        url2.setTimeout("5000"); // 延时5s
        proxy.setUrl(Userservice.class.getName(),url2);
        proxy.refer();

        BlogService blogService = (BlogService) proxy.getProxy(BlogService.class);
        Userservice userservice = (Userservice) proxy.getProxy(Userservice.class);

        CompletableFuture<String> future = userservice.sayHelloAsync("myrpc");
        future.whenComplete((result,e)->{
            if (e != null){
                System.out.println("业务抓到了异常" + e);
                return;
            }
            System.out.println(result);
        });

        Thread.sleep(1000000);
    }
}
