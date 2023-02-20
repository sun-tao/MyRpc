package github.rpc.util;

import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.UUID;

public class GenerateMessageID {
    // 生成2字节的消息ID
    static public byte[] getMessageId(){
        return UUID.randomUUID().toString().replace("-", "").substring(0, 2).getBytes();
    }
}
