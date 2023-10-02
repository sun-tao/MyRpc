import github.rpc.cluster.Cluster;
import github.rpc.util.TimerEntry;

public class TimerTask1 implements Runnable, Cloneable {
    private int delayMs;
    private transient TimerEntry timerEntry;
    public TimerTask1(int delayMs){
        this.delayMs = delayMs;
    }

    @Override
    public void run() {

    }


}
