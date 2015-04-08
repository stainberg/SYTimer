package org.stainberg.interactframework.core.timer;

import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by stainberg on 9/24/14.
 * 计时器单例模型，用于调度传入的计时器事件
 */
public class SYTimer {
    private static SYTimer instance;
    private List<SYTimerTask> tasks;
    private ComparatorTask comparatorTask;
    private Thread thread;
    private STATUS status = STATUS.IDLE;
    private ExecutorService pool;
    private boolean isWaked;

    private SYTimer() {
        tasks = new ArrayList<SYTimerTask>();
        comparatorTask = new ComparatorTask();
        pool = Executors.newFixedThreadPool(10);
        thread = new Thread(r);
        thread.setName("SYTimer");
        thread.start();
    }

    /**
     *
     * @return {@link SYTimer} instance
     * 获取计时器的实例
     */
    public static SYTimer getInstance() {
        if(instance == null) {
            synchronized (SYTimer.class) {
                if(instance == null) {
                    instance = new SYTimer();
                }
            }
        }
        return instance;
    }

    /**
     * 检查计时器线程是否在运行
     */
    private void checkThread() {
        if(status == STATUS.STOP) {
            thread = new Thread(r);
            thread.start();
        }
    }

    /**
     * 计时器主线程，用于计时器时间调度
     */
    private Runnable r = new Runnable() {
        @Override
        public void run() {
            while(!thread.isInterrupted()) {
                if(tasks.isEmpty()) {
                    try {
                        status = STATUS.WAITING;
                        synchronized (SYTimer.class) {
                            SYTimer.class.wait();
                        }
                    } catch (InterruptedException e) {
                        status = STATUS.STOP;
                        thread.interrupt();
                        return;
                    }
                } else {
                    try {
                        synchronized (SYTimer.class) {
                            SYTimerTask task = tasks.get(0);
                            long wait = task.getSurplus();
                            if(wait > 0) {
                                isWaked = false;
                                SYTimer.class.wait(wait);
                                if(isWaked) {
                                    continue;
                                }
                            }
                            pool.execute(task);
                            tasks.remove(task);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        status = STATUS.STOP;
                        thread.interrupt();
                        return;
                    }
                }
                status = STATUS.RUNNING;
            }
        }
    };

    /**
     *
     * @param task 计时器事件 参阅{@link SYTimerTask}
     * 添加一个计时器事件
     */
    public void addTask(SYTimerTask task) {
        synchronized (SYTimer.class) {
            checkThread();
            tasks.add(task);
            Log.d("addTask","now = " + SystemClock.elapsedRealtime()  +  "addTask time to " + task.getWhen());
            Collections.sort(tasks, comparatorTask);
            isWaked = true;
            SYTimer.class.notifyAll();
        }
    }

    /**
     *
     * @param task 计时器事件 参阅{@link SYTimerTask}
     * 取消一个计时器事件
     */
    public void cancelTask(SYTimerTask task) {
        if(task == null) {
            return;
        }
        task.cancel();
        synchronized (SYTimer.class) {
            if(tasks.contains(task)) {
                tasks.remove(task);
                isWaked = true;
                SYTimer.class.notifyAll();
            }
        }
    }

    /**
     *
     * @param name 计时器名字，唯一ID
     * 以计时器名字进行取消一个计时器事件
     *现在只是删除了列表里面最近的一个以name命名的定时器。所以请在给定时器命名时采用唯一值，或者请不要指定定时器名字
     */
    //FIXME now will remove the timer what the first get from the list.
    public void cancelTask(String name) {
        SYTimerTask task = getTaskByName(name);
        if(task == null) {
            return;
        }
        task.cancel();
        synchronized (SYTimer.class) {
            if(tasks.contains(task)) {
                tasks.remove(task);
                isWaked = true;
                SYTimer.class.notifyAll();
            }
        }
    }

    /**
     *
     * @param position 位置
     * 取消一个时间轴上的计时器事件，按计时器在时间轴上的位置计算
     */
    public void cancelTask(int position) {
        synchronized (SYTimer.class) {
            if(tasks.size() > position) {
                tasks.get(position).cancel();
                tasks.remove(position);
                isWaked = true;
                SYTimer.class.notifyAll();
            }
        }
    }

    /**
     *
     * @param l 计时器接口
     * @param timeMillis 延迟时间，毫秒单位
     * 添加一个计时器事件接口，接口请参阅{@link SYTimerListener}
     * 如果添加的是一个已经存在的计时器接口，则会更新当前存在的计时器接口，以新传入的{@param timeMillis}参数重新开始计时。
     */
    public void addNotify(final SYTimerListener l, long timeMillis) {
        SYTimerTask task = getTaskByName(String.valueOf(l.getClass().hashCode()));
        if(task == null) {
            addTask(new SYTimerTask(timeMillis, String.valueOf(l.getClass().hashCode())) {
                @Override
                public void run() {
                    l.onNotify();
                }

                @Override
                public void onTimeOver() {
                    synchronized (SYTimer.class) {
                        isWaked = true;
                        SYTimer.class.notifyAll();
                    }
                }
            });
        } else {
            updateTask(task, timeMillis);
        }
    }

    public void addNotify(final SYTimerListener l, long timeMillis, SYTimerTask.SYTimerStepListener stepListener, long step) {
        SYTimerTask task = getTaskByName(String.valueOf(l.getClass().hashCode()));
        if(task == null) {
            addTask(new SYTimerTask(timeMillis, String.valueOf(l.getClass().hashCode()), stepListener, step) {
                @Override
                public void run() {
                    l.onNotify();
                }

                @Override
                public void onTimeOver() {
                    synchronized (SYTimer.class) {
                        isWaked = true;
                        SYTimer.class.notifyAll();
                    }
                }
            });
        } else {
            updateTask(task, timeMillis);
        }
    }

    /**
     *
     * @param task 计时器任务
     * @param timeMillis 延迟时间
     * 本地方法，用于更新计时器的时间
     */
    private void updateTask(SYTimerTask task,long timeMillis ) {
        synchronized (SYTimer.class) {
            task.setOffsetTimeMillis(timeMillis);
            Log.d("updateTask","now = " + SystemClock.elapsedRealtime()  + "update time to " + task.getWhen());
            isWaked = true;
            SYTimer.class.notifyAll();
        }
    }

    /**
     *
     * @param l
     * 取消一个计时器事件
     */
    public void cancelNotify(SYTimerListener l) {
        cancelTask(String.valueOf(l.getClass().hashCode()));
    }

    private SYTimerTask getTaskByName(String name) {
        for(SYTimerTask item:tasks) {
            if(item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 计时器主线程状态
     */
    public enum STATUS {
        IDLE,
        RUNNING,
        WAITING,
        STOP
    }

    private class ComparatorTask implements Comparator<org.stainberg.interactframework.core.timer.SYTimerTask> {
        @Override
        public int compare(org.stainberg.interactframework.core.timer.SYTimerTask lhs, org.stainberg.interactframework.core.timer.SYTimerTask rhs) {
            return (int)(lhs.getSurplus() - rhs.getSurplus());
        }

        @Override
        public boolean equals(Object object) {
            return false;
        }
    }
}
