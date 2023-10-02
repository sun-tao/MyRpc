package github.rpc.util;

public class TimerEntry implements Cloneable {
    public TimerEntry next;
    public TimerEntry prev;
    private TimerTask timerTask;
    private long expirationTime;

    public TimerEntry(TimerTask timerTask,long expirationTime){
        this.timerTask = timerTask;
        this.expirationTime = expirationTime;
        if (timerTask != null){
            timerTask.setTimerEntry(this);
        }
    }

    public void setTimerTask(TimerTask timerTask){
        this.timerTask = timerTask;
    }
    public long getExpirationTime(){
        return expirationTime;
    }

    public TimerTask getTimerTask(){
        return timerTask;
    }

    @Override
    public TimerEntry clone() {
        // 拷贝链从timertask开始，这里不能再拷贝timertask了，否则将出现环,因此这里直接对timertask做浅拷贝
        try {
            TimerEntry clone = (TimerEntry) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
