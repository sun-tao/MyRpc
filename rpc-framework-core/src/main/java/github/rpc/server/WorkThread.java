package github.rpc.server;



import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Map;

public class WorkThread implements Runnable {
    private Socket socket;
    private Map<String,Object> serviceProvider;
    public WorkThread(Socket socket,Map<String,Object> serviceProvider){
        this.socket = socket;
        this.serviceProvider = serviceProvider;
    }

    public void run() {
        // 执行通信任务
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            String interfaceName = rpcRequest.getInterfaceName();

            // 通过serviceProvider获取真正的serivce对象
            System.out.println(interfaceName);
            Object service = serviceProvider.get(interfaceName);

            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
            Object invoke = method.invoke(service, rpcRequest.getParams());

            System.out.println("客户端请求了" + method.getName()+ "方法");

            RpcResponse response = RpcResponse.success(invoke,rpcRequest.getRequestId());
            objectOutputStream.writeObject(response);
            objectOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
