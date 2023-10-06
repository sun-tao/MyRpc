package github.rpc.remoting;

import github.rpc.common.URL;
import github.rpc.util.NamedThreadFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
// myrpc 框架线程池管理
public class ExecutorRepository {
    private static Map<String, ExecutorService> executorServiceMap = new ConcurrentHashMap<>();

    public static ExecutorService createIfAbsent(String side){ // 目前ExecutorService仅分为provider和consumer两个粒度
        ExecutorService executorService = executorServiceMap.computeIfAbsent(side, k->createExecutor(side));
        return executorService;
    }

    private static ExecutorService createExecutor(String side){
        return Executors.newFixedThreadPool(8,new NamedThreadFactory(side));
    }

}
