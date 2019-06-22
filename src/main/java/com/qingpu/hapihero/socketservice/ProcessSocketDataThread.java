package com.qingpu.hapihero.socketservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qingpu.hapihero.common.utils.DataProcessUtils;
import com.qingpu.hapihero.common.utils.QingpuConstants;
import com.qingpu.hapihero.device.dao.ICoordinatorDeviceDao;
import com.qingpu.hapihero.device.dao.IEndDeviceDao;
import com.qingpu.hapihero.device.dao.IRastberryDeviceDao;
import com.qingpu.hapihero.device.entity.EndDevice;
import com.qingpu.hapihero.socketservice.ClientRastberryDeviceSocket.EndDeviceItem;

public class ProcessSocketDataThread extends Thread{
	private Socket client;
	private IRastberryDeviceDao rastberryDao;
	private ICoordinatorDeviceDao coordDao;
	private IEndDeviceDao endDao;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public ProcessSocketDataThread() {
		// empty
	}
	
	public ProcessSocketDataThread(Socket client, IRastberryDeviceDao rastberryDao, ICoordinatorDeviceDao coordDao, IEndDeviceDao endDao) {
		this.client = client;
		this.rastberryDao = rastberryDao;
		this.coordDao = coordDao;
		this.endDao = endDao;
	}

	@Override
	public void run() {
		logger.debug("@@new socket connection: ip = " + client.getInetAddress() + ", port = " + client.getPort());
		try {
			InputStream in = client.getInputStream();
			byte[] result = new byte[0];
			int tmp = -1;
			boolean header = false;
			boolean tailer = false;
			int count = 0;

			while (!this.isInterrupted()) {
				try {
					Thread.sleep(10); // 10ms，后面需要进行优化，使用阻塞代替睡眠
					count++;
					if(count >= 600) { // 如果连接之后指定的时间内未发送数据，则断开此空连接
						this.closeClient();
						logger.debug("@@close empty connect socket, ip = " + client.getInetAddress() + ", port = " + client.getPort());
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				while ((tmp = in.read()) != -1) {
					count = 0;
					byte b = (byte) tmp;
					if(b == QingpuConstants.HEADER_BYTE){
						if(!header){//收到消息开始字节
							header = true;
						}else{//收到结束字节
							tailer = true;
						}
					}
					result = DataProcessUtils.appendByte(result, b);
					if(header &&  tailer){//如果既收到头又收到尾，则收到一条完整信息，开始处理
						header = false;
						tailer = false;
						handleReceivedData(result); // 消息数据格式：0x7E + 一个字节cmd + 一个字节加密方式0x00 + 一个字节消息体长度length + 消息体 + 校验字节 + 0x7E，除了首尾特殊数据需要替换，0x7E换成0x7D+0x02，0x7D换成0x7D+0x01 
						result = new byte[0];// 清空上次接收的数据
					}
				}
			}
			logger.debug("@@got interrupted exit while(1), ip = " + client.getInetAddress() + ", port = " + client.getPort());
		} catch (IOException e) {
			logger.debug("@@exception socket coksed = " + e.getMessage() + ", this = " + this.getClient());
		}
	}
	
	// 关闭连接socket和销毁线程
	public void closeClient() {		
		try {			
			logger.debug("@@closeClient()货柜线程关闭主线程原来的连接和线程资源");
			this.interrupt(); // 关闭当前线程
			if (client != null) {
				if(!client.isClosed()){
					client.getInputStream().close();
				}
				if(!client.isClosed()){
					client.getOutputStream().close();
				}				
				if(!client.isClosed()){
					client.close();
				}											
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Socket getClient() {
		return client;
	}

	public void setClient(Socket client) {
		this.client = client;
	}
	
	/**
	 * 按照协议处理socket接收的数据，包含树莓派，协调器数据
	 * */
	private void handleReceivedData(byte[] result) {
		byte cmd = 0;
		// 1.转义还原
		byte[] data = DataProcessUtils.replaceData(result);
		// 2.校验验证码
		boolean check = DataProcessUtils.checkMessage(data);
		if (check) {
			cmd = result[1];
			byte[] content = getDecryptedContent(result);
			synchronized (ServerSocketThread.rastberryMachineMap) {
				//*****************************************
				//树莓派命令处理
				//*****************************************
				if(cmd == QingpuConstants.RASTBERRY_REG) { // 树莓派注册
					JSONObject jsonobj = new JSONObject(new String(content));
					String machineId = jsonobj.getString("machineId"); // 获取机器编号				
					
					ClientRastberryDeviceSocket deviceSocket = ServerSocketThread.rastberryMachineMap.get(machineId);
					if(deviceSocket != null) {
						logger.debug("@@rastberry re-reg");						
						deviceSocket.getProcessDataThread().closeClient(); // 先关闭原来的socket和线程
					} else {
						logger.debug("@@rastberry first reg");
						deviceSocket = new ClientRastberryDeviceSocket();						
						deviceSocket.setMachineId(machineId);
						deviceSocket.setEndDeviceItemList(new ArrayList<ClientRastberryDeviceSocket.EndDeviceItem>()); // 初始化终端设备列表
					}
					deviceSocket.setClient(getClient());
					deviceSocket.setProcessDataThread(this);
					deviceSocket.setPreHeartDate(new Date());
					deviceSocket.setTimeout(false); // 设置为没有超时状态
					
					deviceSocket.setCoorOnline(false); // 初始时协调器离线，协调器发送心跳时置为上线状态
					deviceSocket.setCoorNetworkReady(false); // 初始网络置为无状态
					
					ServerSocketThread.rastberryMachineMap.put(machineId, deviceSocket); // 将树莓派连接对象接入map中				
				} else if(cmd == QingpuConstants.RASTBERRY_HEART_BEAT) { // 树莓派心跳
					ClientRastberryDeviceSocket deviceSocket = ServerSocketThread.getRastDeviceConnectObj(getClient());
					if(deviceSocket != null) {
						deviceSocket.setPreHeartDate(new Date());
						deviceSocket.setClient(this.getClient());
						ServerSocketThread.rastberryMachineMap.put(deviceSocket.getMachineId(), deviceSocket);
					}
				}
				//*****************************************
				//协调器命令处理
				//*****************************************
				else if(cmd == QingpuConstants.COORD_HEART_BEAT) { // 协调器心跳
					ClientRastberryDeviceSocket deviceSocket = ServerSocketThread.getRastDeviceConnectObj(getClient());
					if(deviceSocket != null) {
						deviceSocket.setCoorOnline(true); // 设置为在线状态
						deviceSocket.setCoorPreHeartDate(new Date()); // 更新收到心跳的时间
						ServerSocketThread.rastberryMachineMap.put(deviceSocket.getMachineId(), deviceSocket);
					}
				} else if(cmd == QingpuConstants.COORD_NETWORK_READY) { // 协调器网络准备好
					ClientRastberryDeviceSocket deviceSocket = ServerSocketThread.getRastDeviceConnectObj(getClient());
					if(deviceSocket != null) {
						deviceSocket.setCoorNetworkReady(true); // 设置协调器网络准备完毕
						ServerSocketThread.rastberryMachineMap.put(deviceSocket.getMachineId(), deviceSocket);
					}
				}
				//*****************************************
				//终端命令处理
				//*****************************************
				else if(cmd == QingpuConstants.END_JOIN_NETWORK // 终端设备加入网络，消息内容：一个字节deviceNum + 2字节短地址 + 8个字节长地址
						|| cmd == QingpuConstants.END_OFFLINE	// 终端设备离线，消息内容：deviceNum
						|| cmd == QingpuConstants.END_ONLINE_AGAIN) // 终端再次上线：deviceNum + 2字节短地址 
				{
					ClientRastberryDeviceSocket deviceSocket = ServerSocketThread.getRastDeviceConnectObj(getClient());
					if(deviceSocket != null) {
						boolean findDevItem = false;
						int index = -1;
						EndDeviceItem devItem = null;
						
						List<EndDeviceItem> endItemList = deviceSocket.getEndDeviceItemList();
						for(int i = 0; i < endItemList.size(); i++) {
							EndDeviceItem item = endItemList.get(i);
							if(item.getDeviceNum() == content[0]) { // 根据设备编号找到该终端设备
								findDevItem = true;
								index = i;
								devItem = item;
								break;
							}
						}
						if(findDevItem) { // 如果在列表中找到设备
							if(cmd == QingpuConstants.END_JOIN_NETWORK) {
								devItem.setOnline(true);
								System.arraycopy(content, 1, devItem.getShortAddr(), 0, 2); // 复制短地址到数组中
								System.arraycopy(content, 3, devItem.getLongAddr(), 0, 8); // 长地址复制到数组中																
							} else if(cmd == QingpuConstants.END_OFFLINE) {
								devItem.setOnline(false);
							} else if(cmd == QingpuConstants.END_ONLINE_AGAIN) {
								devItem.setOnline(true);
								System.arraycopy(content, 1, devItem.getShortAddr(), 0, 2); // 复制短地址到数组中
							}
							endItemList.add(index, devItem); // 更新元素
						} else {
							devItem = deviceSocket.new EndDeviceItem();
							
							if(cmd == QingpuConstants.END_JOIN_NETWORK) {
								devItem.setOnline(true);
								devItem.setDeviceNum(content[0]);
								System.arraycopy(content, 1, devItem.getShortAddr(), 0, 2); // 复制短地址到数组中
								System.arraycopy(content, 3, devItem.getLongAddr(), 0, 8); // 长地址复制到数组中																
							} else if(cmd == QingpuConstants.END_OFFLINE) {
								devItem.setOnline(false);
							} else if(cmd == QingpuConstants.END_ONLINE_AGAIN) {
								devItem.setOnline(true);
								System.arraycopy(content, 1, devItem.getShortAddr(), 0, 2); // 复制短地址到数组中
							}
							endItemList.add(devItem); // 添加新的元素
						}
					}					
				} else if(cmd == QingpuConstants.END_OFFLINE) { // 终端设备离线命令 
					
				} else if(cmd == QingpuConstants.END_ONLINE_AGAIN) { // 
					
				}
			}
		}		
	}
	
	/**
	 * 从接受的数据中解析出消息体，并根据加密方式进行解密，返回解密之后的数据
	 * */
	private byte[] getDecryptedContent(byte[] result){
		// 获取消息体长度
		byte bodyLength = result[3];
		if(bodyLength > 0){
			// 根据消息体长度获取消息体内容
			byte[] content = new byte[bodyLength];
			// 去除消息头数据，将消息体数据复制到content数组中
			System.arraycopy(result, 4, content, 0, bodyLength);
			// 如果消息体不为空再判断消息体的加密方式
			if (result[3] != (byte) QingpuConstants.ENCRYPT_BY_NONE) {
				logger.debug("@@error, Unknown Encrypt Type");
			}
			return content;
		}
		return null;
	}
}
