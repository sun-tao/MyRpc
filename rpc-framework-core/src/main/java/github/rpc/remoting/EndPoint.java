package github.rpc.remoting;

public interface EndPoint {
    void send(Channel channel,Object message);
}
