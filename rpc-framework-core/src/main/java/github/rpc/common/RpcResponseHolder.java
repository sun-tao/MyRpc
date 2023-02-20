package github.rpc.common;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcResponseHolder {
    // map的大小要进行限制，防止oom
    private ConcurrentHashMap<Integer, CompletableFuture<RpcResponse>> map = new ConcurrentHashMap<Integer, CompletableFuture<RpcResponse>>();

    public void put(int rpcRequestId,CompletableFuture<RpcResponse> future){
        map.put(rpcRequestId,future);
    }

    public void complete(RpcResponse rpcResponse){
        if (rpcResponse.getMessageType() == 1){
            // 心跳包不注册
            return;
        }
        CompletableFuture<RpcResponse> future = map.remove(rpcResponse.getRequestId());
        if (future != null){
            future.complete(rpcResponse);
        }else{
            log.warn("Response找不到匹配的Request");
        }
    }

    public CompletableFuture<RpcResponse> getFuture(String requestId){
        return map.get(requestId);
    }
}
