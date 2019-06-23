package com.qingpu.hapihero.socketservice;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qingpu.hapihero.device.dao.IEndDeviceDao;
import com.qingpu.hapihero.device.dao.IRastberryDeviceDao;

/**
 * socket服务类，接收客户端的请求并处理连接，用于和货柜相连接
 * */
public class ServerSocketThread extends Thread{
	private ServerSocket serverSocket;
	private static final int SERVERPORT = 18888;
	private IRastberryDeviceDao rastberryDao;
	private IEndDeviceDao endDao;

	public static Map<String, ClientRastberryDeviceSocket> rastberryMachineMap = new HashMap<String, ClientRastberryDeviceSocket>(); // 用于存储售货机器人的socket连接，key值为机器上传的心跳中包含的编号值
	private Logger logger = LoggerFactory.getLogger(this.getClass());
		
	public ServerSocketThread(IRastberryDeviceDao rastberryDao, IEndDeviceDao endDao){
		try {
			if (null == serverSocket) {
				this.serverSocket = new ServerSocket(SERVERPORT);  // "120.24.175.156", 9877
			}
			this.rastberryDao = rastberryDao;
			this.endDao = endDao;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据连接的socket通道对象获取注册时封装的RobotClientSocket对象
	 * 未找到返回null
	 * */
	public static ClientRastberryDeviceSocket getRastDeviceConnectObj(Socket client){
		ClientRastberryDeviceSocket valueObj = null;
		
		// 遍历Map对象
		Iterator<Entry<String, ClientRastberryDeviceSocket>> it0 = ServerSocketThread.rastberryMachineMap.entrySet().iterator();
		while(it0.hasNext()) {
			Entry<String, ClientRastberryDeviceSocket> entry = it0.next();
			valueObj = entry.getValue();
			if(valueObj.getClient() == client) {
				return valueObj;
			}
		}		
		return valueObj;
	}
	
	/**
	 * 发送数据到指定的Socket对象
	 * */
	public static int sendDataToRastDeviceSocket(Socket client, byte[] dataT) {
		try {
			if(client != null && client.isConnected()){
				System.out.println("@@发送货柜出货命令 = " + new String(dataT));
				OutputStream out = client.getOutputStream();
				out.write(dataT);
				out.flush();
				return 0;
			}else{
				System.out.println("@@货柜连接Socket通道断开");				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public void run(){
		try {			
			logger.debug("@@FxiedRobotSys Socket started, port = " + SERVERPORT);
			new ProcessHeartBeatClientThread().start(); // 启动货柜连接心跳监测线程
			while(!this.isInterrupted()){
				//如果主socket没有被中断
				Socket client = serverSocket.accept();//阻塞等待客户端的连接
				client.setTcpNoDelay(true);//立即发送数据
				client.setKeepAlive(true);//当长时间未能发送数据，服务器主动断开连接
				client.setSoTimeout(6000); // 设置读取阻塞的超时时间
				//创建新的客户端线程处理请求，如果请求鉴权通过就加入到在线客户端列表中，如果不通过则销毁
				ProcessSocketDataThread client_thread = new ProcessSocketDataThread(client, rastberryDao, endDao);
				//启动子线程
				client_thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeSocketService() {
		// 关闭socket
		try {
			this.interrupt();
			if(null != serverSocket && !serverSocket.isClosed()){
				serverSocket.close();
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//A.创建一个线程处理心跳超时的客户端
	public class ProcessHeartBeatClientThread extends Thread{
		@Override
		public void run(){			
			while(true){				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				synchronized (ServerSocketThread.rastberryMachineMap) {
					Iterator<Entry<String, ClientRastberryDeviceSocket>> it = ServerSocketThread.rastberryMachineMap.entrySet().iterator();
					while(it.hasNext()){
						Entry<String, ClientRastberryDeviceSocket> entry = it.next();
						String key = entry.getKey();//机器编号
						ClientRastberryDeviceSocket beat = entry.getValue();//消息回复对象
						//如果当前时间 - 上一次收到心跳的时间 >= 3000ms
						if((new Date().getTime() - beat.getPreHeartDate().getTime()) >= 1000*5){ //秒	
							if(!beat.isTimeout()) { // 如果还没有设置为超时
								logger.debug("@@货柜socket线程心跳超时，移除客户端 machineId = " + key);
								beat.setTimeout(true);
								beat.getProcessDataThread().closeClient();//关闭连接socket和释放线程
								// it.remove();//从在线列表中移除								
							}
						}
					}
				}	
			}
		}
	}
}
