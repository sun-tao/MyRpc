package github.rpc.serializer;


public interface Serializer {
    // 序列化方法
    byte[] serialize(Object obj);

    //反序列化方法
    Object deserialize(byte[] bytes, int messageType);

    // 获取当前序列化器的类型
    int getSerializerType();

    static Serializer getSerializerByType(int serializerType){
        switch (serializerType){
            case 0:
                return new ObjectSerializer();
            case 1:
                return new HessianSerializer();
            default:
                return null;
        }
    }

}
