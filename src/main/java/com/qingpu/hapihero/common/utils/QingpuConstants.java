package com.qingpu.hapihero.common.utils;

/**
 * 定义一些全局数据常量
 * */
public class QingpuConstants {

	public static final String ORIGINAL_APPID = "wxefee5a4d8684fe7c"; // 微信服务号原来的appid和secret
	public static final String ORIGINAL_APPSECRET = "3aec5a64f46ee29fbe7ed2b3ac1aea36";
	
	public static final byte HEADER_BYTE = 0x7e;
	public static final byte ENCRYPT_BY_NONE = 0x00;
	
	// 树莓派发送的数据类型
	public static final byte RASTBERRY_REG = 0x10; // 注册
	public static final byte RASTBERRY_HEART_BEAT = 0x11; // 心跳
	
	// 协调器发送消息
	public static final byte COORD_HEART_BEAT = 0x21; // 心跳
	public static final byte COORD_NETWORK_READY = 0x22; // 协调器网络组建好	
	
	// 收到关于终端的消息
	public static final byte END_JOIN_NETWORK = 0x30; // 收到终端加入协调器网路的消息
	public static final byte END_REPLY_RECVED_COIN = 0x31; // 终端回复收到投币命令
	public static final byte END_OFFLINE = 0x32; // 协调器发送的掉线终端信息
	public static final byte END_ONLINE_AGAIN = 0x33; // 协调器发送的终端掉线又重新上线的命令
	
}
