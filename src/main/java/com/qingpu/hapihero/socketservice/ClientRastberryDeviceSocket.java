package com.qingpu.hapihero.socketservice;

import java.net.Socket;
import java.util.Date;
import java.util.List;

import com.qingpu.hapihero.device.entity.EndDevice;

import lombok.Data;

/**
 * 用来封装树莓派以及连接的协调器终端等设备的状态信息
 * */
@Data
public class ClientRastberryDeviceSocket {
	private Socket client; // 标识当前的socket连接通道
	private ProcessSocketDataThread processDataThread; // 接收并处理数据的子线程
	private String machineId; // 树莓派的机器编号		
	private Date preHeartDate; // 接收上一次心跳的时间	
	private boolean isTimeout; // 当前socket连接是否超时标识，如果超时置位此值同时关闭对应的socket和线程，下次超时检测时不再进行关闭
	
	private List<EndDeviceItem> endDeviceItemList; // 连接的终端设备列表
	
	private boolean coorOnline; // 协调器是否在线
	private boolean coorNetworkReady; // 协调器是否组建好网络
	private Date coorPreHeartDate; // 收到协调器的上一次心跳时间
	
	@Data
	public class EndDeviceItem {
		public EndDeviceItem() {
			this.deviceNum = 0x00;
			this.shortAddr = new byte[2];
			this.longAddr = new byte[8];
		}
		
		private byte deviceNum; // 设备烧录程序时指定的编号
		private byte[] shortAddr; // 设备的短地址
		private byte[] longAddr; // 设备的IEEE长地址
		private boolean online; // 设备是否在线，在线或者离线时协调器会发送相应的消息 
	}		
}
