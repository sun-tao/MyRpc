package github.rpc.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class IOClient {
    public static RpcResponse sendRequest(String ip , int post, RpcRequest rpcRequest){
        try {
            Socket socket = new Socket(ip, post);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            // 客户端需要动态地设定要访问的服务,采用动态代理的方法去构造request(这里由参数构造好了，直接用即可)
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();

            RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();
//            System.out.println(rpcResponse);
            return rpcResponse;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
