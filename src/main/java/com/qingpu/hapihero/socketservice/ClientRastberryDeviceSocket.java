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
	private boolean isTimeout; // 当前树莓派socket连接是否超时标识，如果超时置位此值同时关闭对应的socket和线程，下次超时检测时不再进行关闭		
}
