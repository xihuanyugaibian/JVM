# 看书心得

1. 本章主要介绍Java：Java的优点，Java的技术体系，Java的发展史，Java虚拟机发展史，Java未来的发展，如何自己编译jdk。  
   其中1.1的Java优点，1.2的技术体系，1.3的发展史可以认真看下对Java有个大概的了解。  
   1.4的Java虚拟机介绍，了解一下虚拟机始祖Exact和HotSpot，别的注意一下名字，需要的时候再看。  
   1.5的Java发展可以了解一下，本书第三版在JDK12的背景下写的，目前2023年已经到了JDK20。里面涉及一些新技术可以和现在的jdk进行印证。  
   1.6要动手搞一下，里面还是有很多坑的。
2. 自己编译JDK要点记录
    1. 在JDK11时，OracleJDK和OpenJDK代码实质上已达到完全一致。  
       实际开发中会碰到很多种类的jdk：OracleJDK、Oracle OpenJDK，Amazon Corretto等都是从OpenJDK源码衍生出的发行版。
    2. OpenJDK官网 https://openjdk.org/  
       获取OpenJDK源码 https://hg.openjdk.org/jdk/jdk15/  网页中先点击左边菜单的Browse，再点击zip 即可下载打包好的源码，下载后再加压。否则很慢。
    3. 构建时注意平台，Windows，mac， Linux需要的依赖不同，mac不具备，Windows依赖安装有点难，用的Linux。  
       注意Linux也分了三类:Redhat(Redhat,CentOS等),Debian(Debian,Ubuntu等),其他. 其中Redhat和Debian安装软件的依赖的命令不一致，并且对应依赖的下载位置也不同。  
       要配合源文件中 doc/building.html 进行依赖安装。  
       我用的CentOS7，安装 yum group install “Development Tools”时遇到坑，该组已安装，只不过系统未标记。yum group list 查看yum组的状态。
    4. 环境和依赖准备好后，进行编译。   
       1 注意命令行进入到源文件的根目录下再执行命令,就两个命令有先后顺序。   
       2 bash configure -enable-debug --with-jvm-variants=server  
       3 make images 5. 调试部分我没有看懂。
       

