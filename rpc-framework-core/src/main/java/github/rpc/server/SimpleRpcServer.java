package github.rpc.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SimpleRpcServer implements RpcServer {
    public static final int port = 8100;
    private Map<String,Object> serviceProvider;
    private ThreadPoolExecutor threadPool;
    public SimpleRpcServer(Map<String,Object> serviceProvider){
        this.serviceProvider = serviceProvider;
        // 来了一个任务，首先看核心线程，如果线程池中运行的线程数少于核心线程数，则直接新起线程来执行任务，无论是否有空闲线程
        // 如果线程池的核心线程数满了，则将任务添加到阻塞队列，等待核心线程空闲出来了，再从中取出来执行
        // 如果阻塞队列也满了，但当前线程池中的线程数还没有超过 maximumPoolSize，那就直接新起线程来执行
        // 如果连maximumPoolSize都超过了，那就执行拒绝策略，拒绝策略共有4种：
        // 1.抛异常
        // 2.不新起线程，用当前的线程执行任务
        // 3.抛弃阻塞队列最老的任务，用线程池的线程执行当前任务
        // 4.直接丢弃
        this.threadPool = new ThreadPoolExecutor(5, 1000, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100) {
        });
    }
    public SimpleRpcServer(){
        this.threadPool = new ThreadPoolExecutor(5, 1000, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100) {
        });
    }
    public void setServiceProvider(Map<String,Object> serviceProvider){
        this.serviceProvider = serviceProvider;
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("服务器启动！");
            while(true){
                Socket socket = serverSocket.accept();   // BIO
                threadPool.execute(new WorkThread(socket,serviceProvider));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {

    }
    // BIO的方式设计当前的RPC服务器

}

//    final Userservice userservice = new UserserviceImpl();
//        try {
//                ServerSocket serverSocket = new ServerSocket(8899);
//                System.out.println("服务器启动！");
//
//                while (true){
//final Socket socket = serverSocket.accept();  // 阻塞方式获取通信socket
//        new Thread(new Runnable() {
//public void run() {
//        try {
//        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
//        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
//        // 服务器接收request请求,调用对应方法
//        RpcRequest request = (RpcRequest) objectInputStream.readObject();
//
//        // 解析request请求，获取客户端真正要调用的方法
//        // request的接口类型这边没有用上，也就是说此时只能处理userservice的接口的方法
//        Method method = userservice.getClass().getMethod(request.getMethodName(), request.getParamsType());
//        // 真正调用接口的方法
//        Object result = method.invoke(userservice, request.getParams());
//        RpcResponse response = RpcResponse.success(result);
//        objectOutputStream.writeObject(response);
//        objectOutputStream.flush();
//        } catch (IOException e) {
//        e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//        e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//        e.printStackTrace();
//        } catch (IllegalAccessException e) {
//        e.printStackTrace();
//        } catch (InvocationTargetException e) {
//        e.printStackTrace();
//        }
//        }
//        }).start();
//        }

