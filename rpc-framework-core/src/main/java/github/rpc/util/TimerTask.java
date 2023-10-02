package github.rpc.util;

import github.rpc.serializer.Serializer;

// 接入时间轮体系的媒介，打通时间轮体系和jdk Runnable体系
public abstract class TimerTask implements Runnable,Cloneable {
    private int delayMs;
    private TimerEntry timerEntry;
    public TimerTask(int delayMs){
        this.delayMs = delayMs;
    }

    public int getDelayMs(){
        return delayMs;
    }

    public void setTimerEntry(TimerEntry timerEntry){
        this.timerEntry = timerEntry;
    }
    public TimerEntry getTimerEntry(){
        return timerEntry;
    }

    @Override
    public TimerTask clone() {
        try {
            TimerTask newTimerTask = (TimerTask) super.clone();
            TimerEntry newTimerEntry = this.timerEntry.clone();// 此时timerentry中的timertask对象还是自己，而不是新拷贝出来的对象
            newTimerEntry.setTimerTask(newTimerTask);
            newTimerTask.setTimerEntry(newTimerEntry);
            return newTimerTask;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
