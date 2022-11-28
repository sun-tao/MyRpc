package github.rpc.spring;

import github.rpc.common.SingletonFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class Shutdown implements DisposableBean {
    @Override
    public void destroy() throws Exception {
        System.out.println("执行了");
    }
}
