package github.rpc.serializer;

import java.io.*;

public class ObjectSerializer implements Serializer {
    // 使用java原生的序列化方式

    public byte[] serialize(Object obj) {
        // 利用java io库来将对象转为字节流
        byte[] bytes = null;
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(obj);
            oos.flush();
            bytes = bao.toByteArray();
            bao.close();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;


    }

    public Object deserialize(byte[] bytes,int messageType) {
        // 同理
        Object obj = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public int getSerializerType() {
        return 0;
    }
}
