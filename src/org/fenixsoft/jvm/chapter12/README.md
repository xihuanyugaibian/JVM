## Java内存模型与线程

计算机的运算速度与它的存储和通信子系统的速度差距太大，大量的时间都花费在磁盘IO、网络通信或者数据库访问上，让计算机同时处理几项任务是较有效的使用处理器性能的手段。  
一个服务端同时对多个客户端提供服务，是一个具体的并发应用场景。
衡量一个服务性能的好坏，每秒事务处理数（TPS Transactions Per Second）是重要指标之一，它代表一秒内服务端平均能响应的请求总数，而TPS值与程序并发能力有非常密切的关系。
对于计算量相同的任务，程序线程并发协调的越有条不絮，效率就越高；反之，线程之间频繁争用数据，互相阻塞甚至死锁，将会大大降低程序的并发能力。

### 硬件的效率与一致性

由于计算机的存储设备与处理器的运算速度有着几个数量级的差距，所以现代计算机都加入一层或多层读写速度尽可能接近处理器运算速度的**高速缓存**来作为内存与处理器之间的缓冲。

缓存一致性：在多路处理器系统中，每个处理器都有自己的高速缓存，而它们有共享同意主内存，这种系统称为共享内存多核系统。当多个处理器的运算任务都涉及到同一块主内存时，将可能导致各自的缓存数据不一致。

除了增加高速缓存之外，为了充分利用处理器内部的运算单元，处理器可能会对输入代码进行**乱序执行优化**，处理器会在计算之后将乱序执行的结果重组，保证该结果与顺序执行的结果时一致的，
但并不保证程序中各个语句计算的先后顺序与输入代码中的顺序一致性，**因此如果存在一个计算任务依赖另一个计算任务的中间结果，那么其顺序性并不能靠代码的先后顺序来保证**。

### Java内存模型

#### 主内存与工作内存

**Java内存模型的主要目的是定义程序中的各种变量的访问规则**，即关注在虚拟机中把变量值存储到内存和从内存中取出变量值这样的底层细节。  
此处变量指：实例字段、静态字段、构成数组对象的元素，不包括局部变量和方法参数，因为这些是线程私有的，不会被共享就不存在竞争问题。

Java内存模型规定了所有的变量都存储在主内存中。  
每条线程还有自己的工作内存，线程的工作内存中保存了**该线程使用的变量的主内存副本**，线程对变量的所有操作（读取、赋值等）都必须在工作内存中进行，而**不能直接读写主内存中的数据**。  
不同线程之间也无法直接访问对方工作内存中的变量，**线程间变量值的传递均需通过主内存来完成**。

此处的主内存、工作内存与Java运行时数据区域堆栈并不是同一个层次对内存的划分，两者基本没有关系。
从更基础的层次上，主内存直接对应与物理硬件的内存，而为了获取更好的运行速度虚拟机可能会让工作内存优先存储于寄存器和高速缓存中，因为程序运行时主要访问的是工作内存。

#### 内存间交互操作

Java内存模型定义了8种原子操作。

* lock（锁定）：作用于主内存的变量，把一个变量标识为一条线程独占的状态。
* unlock（解锁）：作用于主内存的变量，把一个处于锁定状态的变量释放出来，释放后的变量才可以被其他线程锁定。
* read（读取）：作用于主内存的变量，它把一个变量的值从主内存传输到线程的工作内存中，一遍随后的load动作使用。
* load（载入）：作用于工作内存的变量，把read操作从主内存得到的变量值放入工作内存的变量副本中。
* use（使用）：使用于工作内存的变量，把工作内存中一个变量的值传递给执行引擎，每当虚拟机遇到一个需要使用变量的值的字节码指令时就会执行这个操作。
* assign（赋值）：作用于工作内存的变量，把一个从执行引擎接收的值赋给工作内存的变量，每当虚拟机遇到一个给变量赋值的字节码指令时就会执行这个操作。
* store（存储）：作用于工作内存的变量，把工作内存一个变量的值传送到主内存中，以便随后的write操作使用。
* write（写入）：作用于主内存的变量，把store操作从工作内存中得到的变量的值放入主内存的变量中。

Java内存模型规定了执行8种操作时必须满足的规则

* read和load，store和write要成对出现，即一个变量的值从主内存传送到了工作内存，工作内存中就要有变量接收，同样一个变量的值从工作内存传送到主内存，主内存要有变量接收。
* 不允许一个线程丢弃最近的assign操作，即变量在工作内存改变了之后必须同步回主内存，也就是 assign操作后跟着store write。
* 不允许一个线程无原因地（没有发生任何assign操作）把数据从线程的工作内存同步回主内存。即store write操作之前要有assign。 结合前面，得出 assign [] store [] write三个操作按顺序是一组，不能单独使用，但是中间可以加入其他操作。
* 一个新的变量只能在主内存中诞生，不允许在工作内存中直接使用一个未被初始化的变量。即use和store操作的变量要是从主内存read load进来的或者被assign过。load/assign use [] assign [use assign] store [] write
* 一个变量同一个时刻只允许一条线程对其进行lock操作，lock操作可以被同一条线程重复执行多次，需要执行相同次数的unlock操作来解锁。
* 如果对一个变量执行lock操作，会清空**工作内存中此变量的值**，在执行引擎使用这个变量前，需要重新load或assign以初始化变量的值。
* 如果一个变量没有被lock，不允许对他执行unlock操作，也不允许去unlock一个被其他线程lock的变量。
* 对一个变量进行unlock之前，必须把此变量同步回主内存。

#### volatile型变量的特殊规则

当一个变量被定义为volatile之后，他将具备两个特性。

1. 保证此变量对所有线程的可见性。当一条线程修改了这个变量的值，新值对于其他线程来说是立即得知的。  
   volatile变量在各个线程的工作内存中是一致的（从物理角度看，各个线程工作内存中volatile变量可以存在不一致的情况，由于每次使用之前都要先刷新，执行引擎看不到不一致的情况），但是**Java里面的运算操作符不是原子性的导致volatile变量的运算在并发下不安全**。  
   由于volatile变量只能保证可见性，在不符合一下两条规则的运算场景中，仍然要通过加锁来保证原子性。
    1. 运算结果并不依赖变量的当前值，或者能够确保只有单一线程修改变量的值。
    2. 变量不需要与其他变量共同参与不变约束。
2. 禁止指令重排序：普通变量仅会保证在该方法的执行过程中所有依赖赋值结果的地方都能获取到正确的结果，而不能保证变量赋值操作的顺序与程序代码中的顺序一致。  
   有volatile修饰的变量，赋值后多执行了一个`lock addl $0x0,(%esp)`操作，`addl $0x0,(%esp)` 把ESP寄存器的值加0是一个空操作，然后lock将本处理器的缓存写入内存，该写入动作会引起别的处理器或者别的内核无效化其缓存。  
   通过这样一个空操作，可让前面volatile变量的修改对其他处理器立即可见。把修改同步到内存，意味着所有之前的操作都已经执行完成，这样就形成了“指令重排序无法越过内存屏障”的效果。

volatile变量读操作的性能消耗与普通变量几乎没有差别，但是写操作则可能慢一些e，因为它需要在本地代码中插入许多内存屏障指令来保证处理器不发生乱序执行。
大多数场景下，volatile的总开销仍然要比锁更低，如果volatile的语义满足使用场景优先使用。

#### 原子性、可见性、有序性

* 原子性：基本数据类型的访问、读写都具备原子性，如果应用场景更大，在synchronized块之间的操作也具备原子性。
* 可见性：当一个线程修改了共享变量的值时，其他线程能够立即得知这个修改。  
  volatile的特殊规则新值能立即同步到主内存，以及每次使用前立即从主内存刷新。  
  synchronized 同步块可见性是由“对一个变量执行unlock操作之前，必须先把此变量同步到主内存中”获得的。  
  final修饰的字段在构造器中一旦被初始化完成，并且没有把this引用传递出去，那么其他线程就能看见final字段的值。
* 有序性：Java语言提供了volatile和synchronized两个关键字来保证线程之间操作的有序性，volatile关键字本身禁止指令重排序，
  synchronized由一个变量在同一个时刻只允许一条线程对其进行lock操作这条规则决定了持有同一个锁的两个同步块只能串行进入。synchronized不能禁止指令重排序。

#### 先行发生原则

先行发生是Java内存模型中定义的两项操作之间的偏序关系。它是判断数据是否存在竞争，线程是否安全的非常有用的手段。

如果两个操作之间的关系不在以下，并且无法从下列规则中推导出来，则它们没有顺序性保障，虚拟机可以随意地进行重排序。

* 程序次序优先
* 管程锁定优先
* volatile变量原则
* 线程启动原则
* 线程终止原则
* 线程中断原则
* 对象终结原则
* 传递性

一个操作“时间上的先发生”不代表这个操作会“先行发生”。时间先后顺序与先行发生原则之间基本没有因果关系，衡量并发安全性的时候不要受时间顺序的干扰，一切必须以先行发生原则为准。

### Java与线程

线程是比进程更轻量级的调度执行单位，可以把一个进程的资源分配和执行调度分开，各个线程即可以共享进程的资源（内存地址、文件IO）又可以独立调度。  
目前线程是Java里面进行处理器资源调度的最基本单位。

#### 线程的实现

##### 内核线程实现(1:1实现)

内核线程（Kernel-Level Thread，KLT）就是直接由操作系统内核(Kernel)支持的线程，这个线程由Kernel来完成线程切换，Kernel通过操纵调度器对线程进行调度，并负责将线程的任务映射到各个处理器上。  
每个KLT可以视为Kernel的一个分身，这样操作系统就有能力同时处理多件事，支持多线程的Kernel就称为多线程内核（Multi-Threads Kernel）

程序一般不会直接使用KLT，而是使用KLT的一种高级接口：**轻量级进程（Light Weight Process，LWP），LWP就是我们通常意义所讲的线程**，由于每个LWP都由一个KLT支持，因此先支持KLT才能有LWP。

由于KLT的支持，每个LWP都成为一个独立的调度单元，即使其中某一个LWP在系统调用中被阻塞了，也不会影响整个进程继续工作。  
由于是基于KLT实现的，所以各种线程操作都需要进行系统调用，而系统调用的代价相对较高，需要在用户态和内核态中来回切换。
其次每个LWP都需要一个KLT的支持，因此LWP要消耗一定的内核资源，因此一个系统支持的LWP数量有限。

##### 用户线程实现(1:N实现)

##### 混合实现(N:M实现)

### Java与协程
   
