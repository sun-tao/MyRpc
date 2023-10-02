package TimeTask;

public class TimerTaskEntry implements Comparable<TimerTaskEntry>{
    //包含一个任务
    public TimerTask timerTask;
    // 任务的过期时间，此处的过期时间设置的过期间隔+系统当前时间（毫秒）
    public Long expirationMs;

    // 当前任务属于哪一个列表
    public TimerTaskList list;
    // 当前任务的上一个任务，用双向列表连接
    public TimerTaskEntry prev;
    public TimerTaskEntry next;


    public TimerTaskEntry(TimerTask timerTask,Long expirationMs) {
        this.timerTask = timerTask;
        this.expirationMs = expirationMs;
        // 传递进来任务TimerTask，并设置TimerTask的包装类
        if(timerTask != null) {
            timerTask.setTimerTaskEntry(this);
        }
    }

    // 任务的取消，就是判断任务TimerTask的Entry是否是当前任务
    public boolean cancel() {
        return timerTask.getTimerTaskEntry() != this;
    }

    // 任务的移出
    public void remove() {
        TimerTaskList currentList = list;
        while(currentList != null) {
            currentList.remove(this);
            currentList = list;
        }
    }
    // 比较两个任务在列表中的位置，及那个先执行
    @Override
    public int compareTo(TimerTaskEntry that) {
        return Long.compare(expirationMs,that.expirationMs);
    }
}