package github.rpc.remoting.exchange;

import github.rpc.annotation.Spi;
import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.URL;
import github.rpc.extension.ExtensionLoader;
import github.rpc.remoting.Channel;
import github.rpc.util.InternalTimer;
import github.rpc.util.MyRpcTimer;
import github.rpc.util.RpcException;
import github.rpc.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

// 一个类即包含holder管理职责
@Slf4j
public class DefaultFuture extends CompletableFuture<Object> {
    private URL url;
    private RpcRequest request;
    private RpcResponse response;  // 为了兼容同步接口
    private static Map<Integer, DefaultFuture> FUTURES = new ConcurrentHashMap<>();
    private static Map<Integer, TimerTask> REGITSTERED_TIMERTASK = new ConcurrentHashMap<>();
    private static MyRpcTimer timer = (MyRpcTimer) ExtensionLoader.getExtensionLoader(InternalTimer.class).getExtension("timer");

    public DefaultFuture(RpcRequest request, URL url) {
        this.url = url;
        this.request = request;
        FUTURES.put(request.getRequestId(), this);
    }

    public static void registerTimerTask(RpcRequest request, TimerTask timerTask){
        REGITSTERED_TIMERTASK.put(request.getRequestId(),timerTask);
    }

    public static void received(Object message) {
        log.info("client receive message:{}",message);
        RpcResponse response;
        if (message instanceof RpcResponse) {
            response = (RpcResponse) message;
            int id = response.getRequestId();
            DefaultFuture future = FUTURES.get(id);
            future.doReceived(message);
            FUTURES.remove(id);
        }
    }

    private void doReceived(Object message) {
        if (message instanceof RpcResponse) {
            this.response = (RpcResponse) message;
            this.complete(response.getData());
            // 从时间轮中将任务移除，同时超时不重试
            timer.remove(REGITSTERED_TIMERTASK.get(this.response.getRequestId()));
        }
    }

    public static void cancel(Object message) {
        RpcRequest request;
        if (message instanceof RpcRequest) {
            request = (RpcRequest) message;
            int id = request.getRequestId();
            DefaultFuture future = FUTURES.get(id);
            future.doCancel();
            FUTURES.remove(id);
        }
    }

    private void doCancel() {
        this.completeExceptionally(new RuntimeException(RpcException.TimeoutException));
    }

    public Object recreate() {
        if (url.getConsumerAsync().equals("true")) {
            return this;
        } else if (url.getConsumerAsync().equals("false")) {
            return response.getData() != null ? response.getData() : response.getException();
        }
        return this;
    }


}
