package github.rpc.support;

public class RpcExceptionAdapter {
    public static RpcException adapter(Exception e){
        return new RpcException(e.toString());
    }
}
