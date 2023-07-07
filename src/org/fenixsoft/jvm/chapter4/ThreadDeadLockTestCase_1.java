package org.fenixsoft.jvm.chapter4;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author zzm
 */
public class ThreadDeadLockTestCase_1 {
    /**
     * 线程死循环演示
     */
    public static void createBusyThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true)   // 第41行
                    ;
            }
        }, "testBusyThread");
        thread.start();
    }

    /**
     * 线程锁等待演示
     */
    public static void createLockThread(final Object lock) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "testLockThread");
        thread.start();
    }

    /**
     * createBusyThread和createLockThread分别创建了一个线程，虽然主线程mian走完了，但是在主线程中创建的testBusyThread和testLockThread两个线程还在执行。
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        //main线程中，线程为Runnable状态，Runnable状态的线程仍会被分配运行时间，但是readBytes()方法检查到流没有更新就会立刻归还执行令牌给操作系统，这种等待只消耗很小的处理器资源。
        br.readLine();
        //新建了一个testBusyThread线程，程序停留在while(true)，线程为Runnable状态 不会归还执行令牌的动作，会在空循环中耗尽操作系统分配给它的执行时间，直到线程切换为止，这种等待会消耗大量的处理器资源。
        createBusyThread();
        br.readLine();
        Object obj = new Object();
        //新建了一个testLockThread线程，线程在等待lock对象的notify()或notifyAll()方法的出现，此时线程为Waiting状态，在重新唤醒前不会被分配执行时间，只要被唤醒，这个线程便能激活继续执行。
        createLockThread(obj);
    }

}
