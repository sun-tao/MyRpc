package github.rpc.remoting.codec;

import github.rpc.common.URL;
import github.rpc.remoting.Codec;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class MyRpcCodec implements Codec {
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
