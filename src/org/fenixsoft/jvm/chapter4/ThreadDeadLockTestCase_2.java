package org.fenixsoft.jvm.chapter4;

/**
 * @author zzm
 * Integer.valueOf()方法出于减少对象创建次数和节省内存的考虑，会对数值为-128-127之间的Integer对象进行缓存，如果valueOf()方法传入的参数在这个范围之内，就直接返回缓存中的对象。
 * 这时《Java虚拟机规范》中明确要求缓存的默认值，实际值可以通过调整参数 java.lang.Integer.IntegerCache.hign这时。
 */
public class ThreadDeadLockTestCase_2 {

    /**
     * 线程死锁等待演示
     */
    static class SynAddRunnalbe implements Runnable {
        int a, b;
        public SynAddRunnalbe(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public void run() {
            synchronized (Integer.valueOf(a)) {
                synchronized (Integer.valueOf(b)) {
                    System.out.println(a + b);
                }
            }
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            new Thread(new SynAddRunnalbe(1, 2)).start();
            new Thread(new SynAddRunnalbe(2, 1)).start();
        }
    }


}
