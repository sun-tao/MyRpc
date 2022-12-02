package github.rpc.loadbalance;

import github.rpc.annotation.Spi;
import github.rpc.common.RpcRequest;

import java.util.List;

@Spi
public interface LoadBalance {
    String loadBalance(List<String> addresses, RpcRequest rpcRequest);
}
