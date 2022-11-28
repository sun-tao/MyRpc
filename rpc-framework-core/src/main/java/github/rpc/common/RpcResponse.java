package github.rpc.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse implements Serializable {
    // 规定了response的数据格式
    private int code;
    private String message;
    private Object data;

    public static RpcResponse success(Object data){
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.code = 200;
        rpcResponse.message = "success";
        rpcResponse.data = data;
        return rpcResponse;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

