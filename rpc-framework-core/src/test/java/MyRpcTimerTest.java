import com.sun.deploy.uitoolkit.impl.fx.AppletStageManager;
import github.rpc.util.MyRpcTimer;
import github.rpc.util.TimerTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyRpcTimerTest {
    private static ExecutorService executorService = Executors.newFixedThreadPool(1);
    private static int tickMs = 100;
    private static MyRpcTimer timer = new MyRpcTimer(System.currentTimeMillis(),tickMs,20);

    public static void main(String[] args) throws InterruptedException {
        TimerTask timerTask1 = new TimerTask(5 * tickMs) {
            @Override
            public void run() {
                System.out.println("执行!!!");
            }
        };
        TimerTask timerTask2 = new TimerTask(420 * tickMs) {
            @Override
            public void run() {
                System.out.println("执行!!!");
            }
        };
        TimerTask timerTask3 = new TimerTask(100 * tickMs) {
            @Override
            public void run() {
                System.out.println("执行!!!");
            }
        };


        executorService.submit(()->{
            while (true){
                timer.advanceClock(System.currentTimeMillis());
                Thread.sleep(tickMs);
            }
        });

        timer.add(timerTask1);
        timer.add(timerTask2);
        timer.add(timerTask3);

        timer.remove(timerTask3);

        Thread.sleep(100000);
    }

}
