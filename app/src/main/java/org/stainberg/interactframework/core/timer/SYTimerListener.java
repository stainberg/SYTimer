package org.stainberg.interactframework.core.timer;

/**
 * Created by stainberg on 9/24/14.
 * 计时器任务接口，作用类似于{@link SYTimerTask}
 * 可以继承该接口，并重写{@link onNotify()}方法，当时间到达触发事件执行onNotify函数
 */
public interface SYTimerListener {
    public void onNotify();
}
