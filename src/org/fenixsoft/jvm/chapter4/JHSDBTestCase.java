package org.fenixsoft.jvm.chapter4;

/**
 * staticObj、instanceObj、localObj存放在哪里？
 * <br/>staticObj对着Test的类型信息存放在方法区，instanceObj随着Test对象实例存放在Java堆，localObj存放在foo方法栈帧的局部变量表中。
 * <br/>由于JHSDB本身对压缩指针的支持有缺陷，建议用64位系统的读者实验时禁用压缩指针，为了加快在内存中搜索对象的速度，建议限制一下Java堆的大小。
 * <br/>-Xmx10m -XX:-UseCompressedOops
 */
public class JHSDBTestCase {

    static class Test {
        static ObjectHolder staticObj = new ObjectHolder();
        ObjectHolder instanceObj = new ObjectHolder();

        void foo() {
            ObjectHolder localObj = new ObjectHolder();
            System.out.println("done");    // 这里设一个断点
        }
    }

    private static class ObjectHolder {}

    public static void main(String[] args) {
        Test test = new JHSDBTestCase.Test();
        test.foo();
    }
}
