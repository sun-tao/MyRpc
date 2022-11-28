package github.rpc.loadbalance;

import java.util.List;

public interface LoadBalance {
    String loadBalance(List<String> addresses);
}
