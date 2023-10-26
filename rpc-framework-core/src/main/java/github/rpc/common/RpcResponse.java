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
    // 版本号机制
    private static final long serialVersionUID = -3911255650485738676L;
    // 规定了response的数据格式
    private int code; // discard
    private String message; // discard
    private Object data;
    private Exception exception;  // 返回了异常
    private int requestId;
    private int messageType; //todo:写心跳机制的时候再使用
//    private byte[] test = new byte[4096];

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public static RpcResponse wrapperResult(Object data, int requestId){
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.code = 200;
        rpcResponse.message = "success";
        rpcResponse.data = data;
        rpcResponse.setRequestId(requestId);
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

    public void clearException(){
        this.exception = null;
    }
}

