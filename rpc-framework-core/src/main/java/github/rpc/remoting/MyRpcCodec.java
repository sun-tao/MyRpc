package github.rpc.remoting;

import github.rpc.common.URL;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

public class MyRpcCodec implements Codec{
    private URL url;
    private MyRpcEncoder encoder;
    private MyRpcDecoder decoder;
    public MyRpcCodec(URL url){
        this.url = url;
        encoder = new MyRpcEncoder(url);
        decoder = new MyRpcDecoder();
    }
    public MessageToByteEncoder getEncoder(){
        return encoder;
    }
    public ByteToMessageDecoder getDecoder(){
        return decoder;
    }
}
