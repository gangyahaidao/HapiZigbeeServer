package com.qingpu.hapihero.socketservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qingpu.hapihero.common.utils.ByteUtils;
import com.qingpu.hapihero.common.utils.DataProcessUtils;
import com.qingpu.hapihero.common.utils.QingpuConstants;
import com.qingpu.hapihero.device.dao.IEndDeviceDao;
import com.qingpu.hapihero.device.dao.IRastberryDeviceDao;

public class ProcessSocketDataThread extends Thread{
	private Socket client;
	private IRastberryDeviceDao rastberryDao;
	private IEndDeviceDao endDao;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public ProcessSocketDataThread() {
		// empty
	}
	
	public ProcessSocketDataThread(Socket client, IRastberryDeviceDao rastberryDao, IEndDeviceDao endDao) {
		this.client = client;
		this.rastberryDao = rastberryDao;
		this.endDao = endDao;
	}

	@Override
	public void run() {
		logger.warn("@@new socket connection: ip = " + client.getInetAddress() + ", port = " + client.getPort());
		try {
			InputStream in = client.getInputStream();
			byte[] result = new byte[0];
			int tmp = -1;
			boolean header = false;
			boolean tailer = false;
			int count = 0;

			while (!this.isInterrupted()) {
				while ((tmp = in.read()) != -1) { // 会阻塞读取
					count = 0;
					byte b = (byte) tmp;
					if(b == QingpuConstants.HEADER_BYTE){
						if(!header){//收到消息开始字节
							header = true;
						}else{//收到结束字节
							tailer = true;
						}
					} 
					if(header) {
						result = DataProcessUtils.appendByte(result, b);
						if(tailer){//如果既收到头又收到尾，则收到一条完整信息，开始处理
							logger.warn("@recv one frame data");
							header = false;
							tailer = false;
							logger.debug("before replace data length = " + result.length + ", string = " + new String(result));
							if(result.length >= 6) {
								handleReceivedData(result); // 消息数据格式：0x7E + 一个字节cmd + 一个字节加密方式0x00 + 一个字节消息体长度length + 消息体 + 校验字节 + 0x7E，除了首尾特殊数据需要替换，0x7E换成0x7D+0x02，0x7D换成0x7D+0x01
							}							
							result = new byte[0];// 清空上次接收的数据
						}
					}										
				}			
			}
			logger.debug("@@interrupted exit while(1), ip = " + client.getInetAddress() + ", port = " + client.getPort());
		} catch (SocketTimeoutException timeout) {
			logger.debug("@read timeout = " + timeout.getMessage());
			this.closeClient();			
		} catch (IOException e) {
			logger.debug("@@exception socket coksed = " + e.getMessage() + ", this = " + this.getClient());
		}
	}
	
	// 关闭连接socket和销毁线程
	public void closeClient() {		
		try {			
			logger.debug("@@closeClient()线程,关闭ServerSocket主线程原来的连接和线程资源");
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
		logger.debug("after replaced data = " + ByteUtils.bytesToHexString(data));
		// 2.校验验证码
		boolean check = DataProcessUtils.checkMessage(data);
		if (check) {
			cmd = data[1];
			byte[] content = getDecryptedContent(data);			
			//*****************************************
			//树莓派命令处理
			//*****************************************
			if(cmd == QingpuConstants.RASTBERRY_REG) { // 树莓派注册
				synchronized (ServerSocketThread.rastberryMachineMap) {
					JSONObject jsonobj = new JSONObject(new String(content));
					String machineId = jsonobj.getString("machineId"); // 获取机器编号				
					
					ClientRastberryDeviceSocket deviceSocket = ServerSocketThread.rastberryMachineMap.get(machineId);
					if(deviceSocket != null) {
						logger.error("@@@@@rastberry re-reg");						
						deviceSocket.getProcessDataThread().closeClient(); // 先关闭原来的socket和线程
					} else {
						logger.debug("@@rastberry first reg");
						deviceSocket = new ClientRastberryDeviceSocket();						
						deviceSocket.setMachineId(machineId);
					}
					deviceSocket.setClient(getClient());
					deviceSocket.setProcessDataThread(this);
					deviceSocket.setPreHeartDate(new Date());
					deviceSocket.setTimeout(false); // 设置为没有超时状态					
					ServerSocketThread.rastberryMachineMap.put(machineId, deviceSocket); // 将树莓派连接对象接入map中
				}
			} else if(cmd == QingpuConstants.RASTBERRY_HEART_BEAT) { // 树莓派心跳
				synchronized (ServerSocketThread.rastberryMachineMap) {
					ClientRastberryDeviceSocket deviceSocket = ServerSocketThread.getRastDeviceConnectObj(getClient());
					if(deviceSocket != null) {
						deviceSocket.setPreHeartDate(new Date());
						deviceSocket.setClient(this.getClient());
						ServerSocketThread.rastberryMachineMap.put(deviceSocket.getMachineId(), deviceSocket);
					}
				}
				// 返回一条心跳到树莓派
				ResponseSocketUtils.sendBytesToSocket(null, this.getClient(), QingpuConstants.REPLY_RASTBERRY_HEART_BEAT, QingpuConstants.ENCRYPT_BY_NONE);
				logger.debug("recv rastberry heartbeat and replay to client");
			}
			//*****************************************
			//终端命令处理
			//*****************************************
			else if(cmd == QingpuConstants.END_REGISTER_TO_SERVER) // 终端设备使用按键Key1注册到数据库，消息内容：一个字节deviceNum
			{
				logger.debug("@end device use key to register to server");
				// 先判断是否存在指定编号的设备，否则新建一条记录
				
			} else if(cmd == QingpuConstants.END_REPLY_RECVED_COIN) { // 终端设备回复收到投币命令，消息内容：一个字节的deviceNum
				logger.debug("@end device replay get coin cmd");				
			}
		} else {
			logger.error("check failed");
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
			if (result[2] != (byte) QingpuConstants.ENCRYPT_BY_NONE) {
				logger.error("@@Error, Unknown Encrypt Type");
			}
			return content;
		}
		return null;
	}
}
