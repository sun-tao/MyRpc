package github.rpc.cluster;

import github.rpc.common.RpcResponse;

public interface FailoverCluster {
    RpcResponse doInvoke();
}
