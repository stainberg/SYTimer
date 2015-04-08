package org.stainberg.interactframework.core.timer;

import android.os.SystemClock;

/**
 * Created by stainberg on 9/24/14.
 * 计时器任务抽象类,作用类似于{@link SYTimerListener}
 * 用于保存计时器事件的状态
 * 可以继承该对象，并重写{@link run()}方法，当时间到达触发事件执行run函数
 */
public abstract class SYTimerTask implements Runnable {

    /**
     * Starts executing the active part of the class' code. This method is
     * called when a thread is started that has been created with a class which
     * implements {@code Runnable}.
     */

    private long createTime;
    private long offsetTimeMillis;
    private long when;
    private String name;
    private SYTimerStepListener stepListener;
    private long step;
    private Thread thread;
    private static Object lock;
    private boolean threadIntterupt;

    protected SYTimerTask() {
        this(1L);
    }

    protected SYTimerTask(long timeMillis) {
        this(timeMillis, String.valueOf(System.nanoTime()));
    }

    protected SYTimerTask(long timeMillis, String name) {
        this(timeMillis, name, null, 0);
    }

    protected SYTimerTask(long timeMillis, SYTimerStepListener stepListener, long step) {
        this(timeMillis, String.valueOf(System.nanoTime()), stepListener, step);
    }

    protected SYTimerTask(long timeMillis, String name, SYTimerStepListener stepListener, long step) {
        createTime = SystemClock.elapsedRealtime();
        offsetTimeMillis = timeMillis;
        when = createTime + offsetTimeMillis;
        this.name = name;
        this.stepListener = stepListener;
        this.step = step;
        if(step > 0 && stepListener != null) {
            lock = new Object();
            threadIntterupt = false;
            thread = new Thread(r);
            thread.setName("SYTimer-Step-Task");
            thread.start();
        }
    }


    final public long getWhen() {
        return when;
    }

    final public String getName() {
        return name;
    }

    final public void setOffsetTimeMillis(long timeMillis) {
        createTime = SystemClock.elapsedRealtime();
        offsetTimeMillis = timeMillis;
        when = createTime + offsetTimeMillis;
    }

    final private Runnable r = new Runnable() {
        @Override
        public void run() {
            int i = 0;
            while (!thread.isInterrupted() && getSurplus() > step && !threadIntterupt) {
                try {
                    synchronized (lock) {
                        lock.wait(step);
                        i++;
                        if(threadIntterupt) {
                            stepListener.onStepError(step * i, offsetTimeMillis);
                            return;
                        }
                        stepListener.onStepNotify(step * i, offsetTimeMillis);
                    }
                } catch (InterruptedException e) {
                    stepListener.onStepError(step * i, offsetTimeMillis);
                    stepListener = null;
                    return;
                }
            }
            long last = getSurplus() - step;
            if(!thread.isInterrupted() && last > 0 && !threadIntterupt) {
                try {
                    synchronized (lock) {
                        lock.wait(last);
                    }
                } catch (InterruptedException e) {
                    stepListener.onStepError(offsetTimeMillis, offsetTimeMillis);
                    stepListener = null;
                    return;
                }
            }
            onTimeOver();
            stepListener = null;
        }
    };

    final public void cancel() {
        if(thread == null) {
            return;
        }
        threadIntterupt = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    final public long getSurplus() {
        return when - SystemClock.elapsedRealtime();
    }

    @Override
    public abstract void run();

    public abstract void onTimeOver();

    public interface SYTimerStepListener {
        public void onStepNotify(long step, long duration);
        public void onStepError(long step, long duration);
    }
}
