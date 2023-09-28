package github.rpc.remoting.client;


import github.rpc.annotation.Spi;
import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.URL;
import github.rpc.remoting.AbstractEndpoint;
import github.rpc.remoting.ChannelHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class AbstractClient extends AbstractEndpoint {
    protected ExecutorService executorService;  //客户端线程池
    public AbstractClient(URL url, ChannelHandler handler) {
        super(url, handler);
        doOpen();
        doConnect();
        executorService = Executors.newFixedThreadPool(10); // todo：线程池参数调优
    }

    public abstract void doOpen();

    public abstract void doConnect();

    public abstract void send(Object message);


}
