package com.qingpu.hapihero.socketservice;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.json.JSONObject;

import com.qingpu.hapihero.common.utils.ByteUtils;
import com.qingpu.hapihero.common.utils.DataProcessUtils;
import com.qingpu.hapihero.common.utils.QingpuConstants;

public class ResponseSocketUtils {		
	
	/**
	 * 发送字节消息体到树莓派
	 * */
	public static void sendBytesToSocket(byte[] bytes, Socket client, byte cmd, byte encryptType) {
		try {
			byte[] sendbytes = buildUpSendBytes(bytes, cmd, encryptType);
			System.out.println("send to client data = " + ByteUtils.bytesToHexString(sendbytes));
			if(client != null && !client.isOutputShutdown())
			{
				OutputStream out = client.getOutputStream();
				out.write(sendbytes);
				out.flush();
			}
		} catch (IOException e) {			
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 将数据组合成协议要求的字节，并进行加密要求进行了响应的处理
	 * */
	private static  byte[] buildUpSendBytes(byte[] content, byte cmd, byte encryptType) throws Exception {
		byte[] dataT = new byte[4];
		byte check = 0x0;		
		dataT[0] = QingpuConstants.HEADER_BYTE;
		dataT[1] = cmd;
		dataT[2] = encryptType;		
		if(content != null) {
			dataT[3] = (byte) content.length;
			dataT = DataProcessUtils.mergeArray(dataT, content);
		} else {
			dataT[3] = 0x0;
		}
		
		//计算校验码
		check = dataT[1];
		for(int i = 2; i < dataT.length; i++){
			check ^= dataT[i];
		}
		//合并校验码字段
		dataT = DataProcessUtils.appendByte(dataT, check);
		//合并尾部标志位
		dataT = DataProcessUtils.appendByte(dataT, QingpuConstants.HEADER_BYTE);
		//替换数据
		//System.out.println("--替换之前的数据 = " + ByteUtils.bytesToHexString(dataT));
		byte[] rd = replaceDataBytes(dataT);
		//System.out.println("--替换之后的数据 = " + ByteUtils.bytesToHexString(rd));
		return rd;
	}
	
	/**
	 * 替换特殊字节
	 * */
	private static byte[] replaceDataBytes(byte[] dataT) {
		// TODO Auto-generated method stub
		//将消息头包括消息体、校验码中的0x7e替换为0x7d 0x02，0x7d替换成0x7d 0x01
		byte[] ret = {dataT[0]};
		for(int i = 1; i < dataT.length-1; i++){
			if(dataT[i] == (byte)0x7e){
				ret = DataProcessUtils.appendByte(ret, (byte)0x7d);
				ret = DataProcessUtils.appendByte(ret, (byte)0x02);
			}else if(dataT[i] == (byte)0x7d){
				ret = DataProcessUtils.appendByte(ret, (byte)0x7d);
				ret = DataProcessUtils.appendByte(ret, (byte)0x01);
			}else{
				ret = DataProcessUtils.appendByte(ret, dataT[i]);
			}
		}
		//将最后一个标志位手动补上
		ret = DataProcessUtils.appendByte(ret, (byte)0x7e);
		
		return ret;
	}
}
