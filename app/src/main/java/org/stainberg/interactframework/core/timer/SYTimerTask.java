package org.stainberg.interactframework.core.timer;

import android.os.SystemClock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    protected SYTimerTask() {
        this(1L);
    }

    protected SYTimerTask(long timeMillis) {
        this(timeMillis, String.valueOf(System.nanoTime()));
    }

    protected SYTimerTask(long timeMillis, String name) {
        createTime = SystemClock.elapsedRealtime();
        offsetTimeMillis = timeMillis;
        when = createTime + offsetTimeMillis;
        this.name = name;
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

    final public long getSurplus() {
        return when - SystemClock.elapsedRealtime();
    }

    @Override
    public abstract void run();
}
