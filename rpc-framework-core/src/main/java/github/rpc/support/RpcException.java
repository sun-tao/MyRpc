package github.rpc.support;

import java.io.Serializable;

public class RpcException extends Exception implements Serializable {
    private String message;
    public RpcException(String message){
        super();
        this.message = message;
    }
}
