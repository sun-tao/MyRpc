package github.rpc.common;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
public class RpcResponseHolder {
    // map的大小要进行限制，防止oom
    private HashMap<String,RpcResponse> map = new HashMap<>();

    public void inject(RpcResponse rpcResponse){
        if (rpcResponse != null){
            map.put(rpcResponse.getRequestId(),rpcResponse);
        }else{
            log.info("返回的结果为null");
        }
    }

    public RpcResponse getRpcResponse(String requestId){
        return map.get(requestId);
    }
}
