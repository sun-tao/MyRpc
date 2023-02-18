package github.rpc.serializer;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianSerializer implements Serializer {
    public byte[] serialize(Object obj) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(os);
        try {
            output.writeObject(obj);   // obj -> byte
            output.getBytesOutputStream().flush();
            output.completeMessage();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return os.toByteArray();
    }

    public Object deserialize(byte[] bytes, int messageType) {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        Hessian2Input input = new Hessian2Input(is);
        Object obj = null;
        try {
            obj = input.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public int getSerializerType() {
        return 1;
    }
}
