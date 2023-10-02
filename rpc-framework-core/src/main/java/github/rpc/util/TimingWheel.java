package github.rpc.util;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
@Slf4j
public class TimingWheel {
    private List<TimerEntryList> buckets;
    private int taskCount;   // 本时间轮中的总任务数
    private long currentTime;
    private TimingWheel overflowTimingWheel;  // 上层时间轮
    private long startMs;
    private int tickMs;  // 单次触发周期
    private int interval; // 单层时间轮的转动周期
    private int wheelSize;
    public TimingWheel(long startMs,int tickMs,int wheelSize){
        this.startMs = startMs;
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.interval = tickMs * wheelSize;
        this.buckets = new ArrayList<>(wheelSize);
        for (int i = 0 ; i < wheelSize ; i++){
            buckets.add(new TimerEntryList(this));
        }
        this.currentTime = startMs / tickMs * tickMs;
    }

    public boolean add(TimerEntry timerEntry) {  // 对本层来说，只负责任务的添加，但是要耦合任务执行功能，因此需要返回boolean值来为上层提供支撑
        long expirationTime = timerEntry.getExpirationTime();
        //log.info("TimingWheel.add: expirationTime({}),currentTime({})",expirationTime,currentTime);
        if (expirationTime < currentTime + tickMs){ // 过期,添加失败，直接执行
            return false;
        }else if (expirationTime < currentTime + interval){ // 未过期，且在本层时间轮中,安排分配
            expirationTime = expirationTime / tickMs * tickMs;
            int virtualId = (int) (expirationTime / tickMs % wheelSize);
            TimerEntryList bucket = buckets.get(virtualId);
            if (bucket == null){
                bucket = new TimerEntryList(this);
            }
            //log.info("TimingWheel.add: currentTime({}) , insert bucket virtualId({})",currentTime, virtualId);
            bucket.add(timerEntry);
            taskCount++;
        }else { // 超出本层时间轮所能容纳的范围，递归的分配到上层中
            if (overflowTimingWheel == null){
                addOverflowTimingWheel(currentTime,interval,wheelSize);
            }
            overflowTimingWheel.add(timerEntry);
        }
        return true;
    }

    private void addOverflowTimingWheel(long currentTime,int tickMs,int wheelSize){
        overflowTimingWheel = new TimingWheel(currentTime,tickMs,wheelSize);
    }

    public boolean remove(TimerEntry timerEntry){
        long expirationTime = timerEntry.getExpirationTime();
        if (expirationTime < currentTime + tickMs){ // 过期了，remove失败
            return false;
        }else if (expirationTime < currentTime + interval){ // 在本层中，直接remove
            expirationTime = expirationTime / tickMs * tickMs;
            int virtualId = (int) (expirationTime / tickMs % wheelSize);
            TimerEntryList bucket = buckets.get(virtualId);
            if (bucket == null){
                bucket = new TimerEntryList(this);
            }
            bucket.remove(timerEntry);
            taskCount--;
        }else{  // 在上层中,递归的remove
            if (overflowTimingWheel == null){
                return false;
            }
            overflowTimingWheel.remove(timerEntry);
        }
        return true;
    }

    public void advanceClock(long timeMs){
        if (timeMs >= currentTime + tickMs){
            // 推进
            currentTime = currentTime + tickMs;
            if (overflowTimingWheel != null){ // 递归的推进上层时间轮的时间
                overflowTimingWheel.advanceClock(timeMs);
            }
        }
    }
    public List<TimerEntryList> getBuckets(){ // 根据currentTime返回已经到期的buckets
        List<TimerEntryList> res = new ArrayList<>();
        // 是否读取下一层的bucket,保证同一个bucket对每个时间轮只读取一次
        if (overflowTimingWheel != null){
            // fixme:必须要递归的向上读
            if (currentTime % interval == 0){
                List<TimerEntryList> buckets1 = overflowTimingWheel.getBuckets();
                res.addAll(buckets1);
//                overflowTimingWheel.getBuckets(res);
            }
        }
        getBuckets(res);
        return res;
    }
    private void getBuckets(List<TimerEntryList> tel){
        int virtualId = (int) (currentTime / tickMs % wheelSize);
        TimerEntryList timerEntryList = buckets.get(virtualId);
        //log.info("TimingWheel.getBuckets: currentTime({}) ,get bucket virtualId({}), bucket size ({})",currentTime, virtualId,timerEntryList.getSize());
        tel.add(timerEntryList);
    }

    public void dropTaskCount(int size){  // 外部函数清理的时候调用，减少总任务数
        taskCount -= size;
    }



}
