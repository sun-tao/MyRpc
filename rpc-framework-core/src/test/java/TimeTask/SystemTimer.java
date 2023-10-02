package TimeTask;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class SystemTimer {
    private String executorName;
    private Long tickMs = 1L;
    private Integer wheelSize = 20;
    private Long startMs = System.currentTimeMillis();
    //用来执行TimerTask任务
    private ExecutorService taskExecutor =
            Executors.newFixedThreadPool(1,(runnable) -> {
                Thread thread = new Thread(runnable);
                thread.setName("executor-" + executorName);
                thread.setDaemon(false);
                return thread;
            });
    //延迟队列
    private DelayQueue<TimerTaskList> delayQueue = new DelayQueue<>();
    private AtomicInteger taskCounter = new AtomicInteger(0);
    private TimingWheel timingWheel;

    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();

    // 用来执行时间轮的重新排列，及上一个槽中的任务列表被执行后，后面的槽中的任务列表移动
    private Consumer<TimerTaskEntry> reinsert = (timerTaskEntry) -> addTimerTaskEntry(timerTaskEntry);

    public SystemTimer(String executorName, Long tickMs, Integer wheelSize, Long startMs) {
        this.executorName = executorName;
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.startMs = startMs;
        this.timingWheel = new TimingWheel(
                tickMs,
                wheelSize,
                startMs,
                taskCounter,
                delayQueue
        );
    }

    // 可能会多个线程操作，所以需要加锁
    public void add(TimerTask timerTask) {
        readLock.lock();
        try{
            addTimerTaskEntry(new TimerTaskEntry(timerTask,timerTask.delayMs + System.currentTimeMillis()));
        }finally {
            readLock.unlock();
        }
    }

    private void addTimerTaskEntry(TimerTaskEntry timerTaskEntry) {      // 往时间轮添加任务
        if(!timingWheel.add(timerTaskEntry)) {
            // 返回false并且任务未取消，则提交当前任务立即执行。
            if(!timerTaskEntry.cancel()) {
                taskExecutor.submit(timerTaskEntry.timerTask);
            }
        }
    }

    // 向前驱动时间轮
    public boolean advanceClock(Long timeoutMs) throws Exception{
        // 使用阻塞队列获取任务
        TimerTaskList bucket = delayQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        if(bucket != null) {
            writeLock.lock();
            try{
                while(bucket != null) {
                    timingWheel.advanceClock(bucket.getExpiration());
                    // 驱动时间后，需要移动TimerTaskList到上一个槽或者从上一层移动到本层
                    bucket.flush(reinsert);
                    bucket = delayQueue.poll();
                }
            }finally {
                writeLock.unlock();
            }
            return true;
        }else {
            return false;
        }
    }

    public int size() {
        return taskCounter.get();
    }

    public void shutdown() {
        taskExecutor.shutdown();
    }
}