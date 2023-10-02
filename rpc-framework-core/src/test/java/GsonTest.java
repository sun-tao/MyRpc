import com.alibaba.fastjson2.JSON;
import com.google.gson.Gson;
import github.rpc.util.MyRpcTimer;
import github.rpc.util.TimerEntry;
import github.rpc.util.TimerTask;
import github.rpc.util.TimingWheel;
import org.junit.Test;

public class GsonTest {
    @Test
    public void test01(){
        Gson gson = new Gson();
        TimerTask1 hhh = new TimerTask1(5);

        Object json = JSON.toJSON(hhh);
        String json1 = gson.toJson(hhh);
        System.out.println(json);
    }
}
