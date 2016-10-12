本程序主要功能为同步考勤机数据至OA系统中

本程序可运行Linux、Windows系统中使用

Linux部署（以树莓派为例）

1、将SynchAttend生成jar包放置到/opt/java目录下
/opt/java

2、编写runjava.sh
export JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm-vfp-hflt
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
echo $JAVA_HOME >> /opt/java/runtest.sh
java -jar /opt/java/SynchAttend.jar

3、配置定时任何
crontab -e
*/1 * * * * echo "hello" >> /opt/java/test.sh      测试用，正式环境不需要
/30 * * * * sh /opt/java/runjava.sh
====================================================================================
功能描述：此jar包通过webservice远程同步考勤数据。
特别注意：树莓派上的时区设置正确
使用说明：1 、通过winRar 打开jar包，修改根目录下的context.properties 文件中的参数：
			#远端服务器ip地址
				webServiceIp=172.20.0.203
			#远端服务器端口号winRar
				webServicePort=8080
			#考勤机的编号
				machineCode=0001（0001为御泽嘉园考勤机，0002为综合办考勤机，0003为世纪佳缘考勤机编码，0004为天津）
	  2、在jar包所在的目录下面新建一个文件夹logs ,用来记录每天的日志文件。
	  3、运行jar包：打开dos命令窗口
			进入jar包目录
			输入 java -jar synchAttend.jar 运行，出现如下日志：表示运行成功：

附：jar包运行成功日志信息如下：
2015-11-10 16:11:24 INFO [main] (SmpReceiveRsponseService.java:153) -  step 0 : smp receive the last attend data from db
2015-11-10 16:11:25 INFO [main] (SmpReceiveRsponseService.java:125) - step 1 : connection the attendMachine successly
2015-11-10 16:11:25 INFO [main] (SmpReceiveRsponseService.java:90) - step 2 :smp encapsulate data to list
2015-11-10 16:11:25 INFO [main] (SmpReceiveRsponseService.java:91) - stpe 2.1 :theList: time is 0 name list 0 id list 0
2015-11-10 16:11:25 INFO [main] (SmpReceiveRsponseService.java:160) -  step 3: smp send data to the webservice