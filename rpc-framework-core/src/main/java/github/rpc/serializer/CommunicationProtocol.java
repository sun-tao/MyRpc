package github.rpc.serializer;

// 通信协议格式
// ## 协议头：魔数(4B) ## 整体长度 (4B) ## 头长度(2B) ## 协议版本 (1B) ## 消息类型(2B) ## 序列化方式(2B) ## 消息ID(2B) ## 头部扩展字段()
// ## 协议体: 数据
public interface CommunicationProtocol {
    int MAGIC_NUMBER = 0x12345678;
    Byte VERSION = 1;

    enum DecodeResult{
        NEED_MORE_INPUT
    }

}
