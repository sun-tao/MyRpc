package TimeTask;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class TimerTaskList implements Delayed {
    //当前列表中包含的任务数
    private AtomicInteger taskCounter;
    // 列表的头结点
    private TimerTaskEntry root;
    // 过期时间
    private AtomicLong expiration = new AtomicLong(-1L);


    public TimerTaskList(AtomicInteger taskCounter) {
        this.taskCounter = taskCounter;
        this.root =  new TimerTaskEntry(null,-1L);
        root.next = root;
        root.prev = root;
    }

    // 给当前槽设置过期时间
    public boolean setExpiration(Long expirationMs) {
        return expiration.getAndSet(expirationMs) != expirationMs;
    }

    public Long getExpiration() {
        return expiration.get();
    }

    // 用于遍历当前列表中的任务
    public synchronized  void foreach(Consumer<TimerTask> f) {
        TimerTaskEntry entry = root.next;
        while(entry != root) {
            TimerTaskEntry nextEntry = entry.next;
            if(!entry.cancel()) {
                f.accept(entry.timerTask);
            }
            entry = nextEntry;
        }
    }

    // 添加任务到列表中
    public void add(TimerTaskEntry timerTaskEntry) {
        boolean done = false;
        while(!done) {
            //  在添加之前尝试移除该定时任务，保证该任务没有在其他链表中
            timerTaskEntry.remove();

            synchronized (this) {
                synchronized (timerTaskEntry) {
                    if(timerTaskEntry.list == null) {
                        TimerTaskEntry tail = root.prev;
                        timerTaskEntry.next = root;
                        timerTaskEntry.prev = tail;
                        timerTaskEntry.list = this;
                        tail.next = timerTaskEntry;
                        root.prev = timerTaskEntry;
                        taskCounter.incrementAndGet();
                        done = true;
                    }
                }
            }
        }
    }

    //移出任务
    public synchronized void remove(TimerTaskEntry timerTaskEntry) {
        synchronized (timerTaskEntry) {
            if(timerTaskEntry.list == this) {
                timerTaskEntry.next.prev = timerTaskEntry.prev;
                timerTaskEntry.prev.next = timerTaskEntry.next;
                timerTaskEntry.next = null;
                timerTaskEntry.prev = null;
                timerTaskEntry.list = null;
                taskCounter.decrementAndGet();
            }
        }
    }

    public synchronized void flush(Consumer<TimerTaskEntry> f) {
        TimerTaskEntry head = root.next;
        while(head != root) {
            remove(head);
            f.accept(head);
            head = root.next;
        }
        expiration.set(-1L);
    }
    //获得当前任务剩余时间
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(Math.max(getExpiration() - System.currentTimeMillis(),0),TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed d) {
        TimerTaskList other = (TimerTaskList) d;
        return Long.compare(getExpiration(),other.getExpiration());
    }
}