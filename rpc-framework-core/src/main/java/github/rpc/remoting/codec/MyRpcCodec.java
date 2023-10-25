package github.rpc.remoting.codec;

import github.rpc.common.URL;
import github.rpc.remoting.Codec;
import github.rpc.remoting.Decoder;
import github.rpc.remoting.Encoder;
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
    public Encoder getEncoder(){
        return encoder;
    }
    public Decoder getDecoder(){
        return decoder;
    }
}
