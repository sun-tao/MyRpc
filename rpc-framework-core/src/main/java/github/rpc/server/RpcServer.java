package github.rpc.server;

import github.rpc.annotation.Spi;

@Spi
public interface RpcServer {
    void start();
    void stop();
}
