#指定基础镜像，在其上进行定制
FROM java:8

MAINTAINER gangyahaidao <736791342@qq.com>

#这里的 /tmp 目录就会在运行时自动挂载为匿名卷，任何向 /tmp 中写入的信息都不会记录进容器存储层
VOLUME /tmp

#复制上下文目录下的target/.jar 到容器里
ADD target/HapiZigbeeServer-0.0.1-SNAPSHOT.jar /HapiZigbeeServer-0.0.1-SNAPSHOT.jar

#声明运行时容器提供服务端口，这只是一个声明，在运行时并不会因为这个声明应用就会开启这个端口的服务
EXPOSE 80

#指定容器启动程序及参数   <ENTRYPOINT> "<CMD>"
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","/HapiZigbeeServer-0.0.1-SNAPSHOT.jar"]