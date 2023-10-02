package github.rpc.util;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
@Slf4j
public class TimerEntryList {
    // 虚拟头结点
    private TimerEntry head = new TimerEntry(null,-1);
    // 虚拟尾结点
    private TimerEntry tail = new TimerEntry(null,-1);
    private TimingWheel timingWheel;
    private int size;

    public TimerEntryList(TimingWheel timingWheel){
        // 初始化虚拟头尾结点
        head.next = tail;
        head.prev = null;
        tail.next = null;
        tail.prev = head;
        this.timingWheel = timingWheel;
    }

    public void add(TimerEntry timerEntry){ // 双向链表添加元素，添加到尾部
        TimerEntry tmp = tail.prev;
        tail.prev = timerEntry;
        timerEntry.next = tail;
        tmp.next = timerEntry;
        timerEntry.prev = tmp;
        size++;
    }

    public boolean remove(TimerEntry timerEntry){ // 返回remove成功或者失败结果
        TimerEntry tmp = head;
        // 二级时间轮的不同的 bucket 共用了同一个 timerEntry对象
        while(tmp != null){
            if (tmp == timerEntry){
                tmp.prev.next = tmp.next;
                tmp.next.prev = tmp.prev;
                size--;
                return true;
            }
            tmp = tmp.next;
        }
        return false;
    }

    public void flush(Consumer<TimerTask> f){ // 耦合上层时间轮的逻辑，将本层时间轮的bucket执行，上层的降级，因此需要上层函数回调
        TimerEntry tmp = head;
        while(tmp != null){
            TimerTask timerTask = tmp.getTimerTask();
            if (timerTask != null){
                // 原先直接将任务对象赋值给accept，浅拷贝，在删除上层时间轮的timerentry的时候把刚插入本层时间轮的timerentry对象也给删了，导致任务降级失败
                // fixed: deepcopy
                TimerTask timerTaskCloned = timerTask.clone();
                f.accept(timerTaskCloned);
                remove(tmp);  // 执行完后从bucket中删掉,同时避免删掉head和tail结点
            }
            tmp = tmp.next;
        }
    }

    public int getSize(){
        return size;
    }

    public TimingWheel getTimingWheel(){
        return timingWheel;
    }




}
