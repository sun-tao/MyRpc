package github.rpc.util;

import github.rpc.Invoker;
import github.rpc.remoting.MyRpcEncoder;
import jdk.nashorn.internal.runtime.Timing;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
@Slf4j
public class MyRpcTimer implements InternalTimer {
    private ExecutorService taskExecutor; //时间轮的任务处理线程池
    private TimingWheel timingWheel;
    private Consumer<TimerTask> f = (timerTask) -> addCallback(timerTask);
    public MyRpcTimer(long startMs,int tickMs,int wheelSize){
        this.timingWheel = new TimingWheel(startMs,tickMs,wheelSize);
        this.taskExecutor = Executors.newFixedThreadPool(8,new NamedThreadFactory("timingWheel"));
    }

    public MyRpcTimer(){
        this.timingWheel = new TimingWheel(System.currentTimeMillis(),100,20);
        this.taskExecutor = Executors.newFixedThreadPool(8);
    }

    public void add(TimerTask timerTask){ // 对外暴露的接口，新增timerTask任务
        // fixme:这里需要对原生的add和回调的add拆分，否则回调的add也将任务的过期时间+当前时间则永远不会触发
        TimerEntry timerEntry = new TimerEntry(timerTask,timerTask.getDelayMs() + System.currentTimeMillis());
        boolean success = timingWheel.add(timerEntry);
        if (!success){ // 任务过期了，直接执行
            taskExecutor.submit(timerTask);
        }
    }

    public void addCallback(TimerTask timerTask){ // 对内添加回调的回调函数
        TimerEntry timerEntry = timerTask.getTimerEntry();
        if (timerEntry == null){
            //log.warn("MyRpcTimer.addCallback: timerEntry is null when callback");
        }else{
            boolean success = timingWheel.add(timerEntry);
            if (!success){ // 任务过期了，直接执行
                taskExecutor.submit(timerTask);
            }
        }
    }

    private void addTimingWheel(){

    }

    public void remove(TimerTask timerTask){ // 对外暴露的接口，取消任务
        if (timerTask == null){
            return;
        }
        timingWheel.remove(timerTask.getTimerEntry());
    }

    public void advanceClock(long timeMs){
        timingWheel.advanceClock(timeMs);
        List<TimerEntryList> buckets = timingWheel.getBuckets(); //获取到期了的buckets
        for (int i = 0 ; i < buckets.size() ; i++){
            TimerEntryList bucket = buckets.get(i);
            int originSize = bucket.getSize();
            bucket.flush(f);  //flush掉之后bucket的size正常了，但是时间轮的任务数还没调整
            TimingWheel tw = bucket.getTimingWheel();
            tw.dropTaskCount(originSize);
        }
    }
}
