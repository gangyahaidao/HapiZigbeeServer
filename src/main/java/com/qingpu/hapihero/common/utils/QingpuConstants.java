package com.qingpu.hapihero.common.utils;

/**
 * 定义一些全局数据常量
 * */
public class QingpuConstants {

	public static final String ORIGINAL_APPID = "wxefee5a4d8684fe7c"; // 微信服务号原来的appid和secret
	public static final String ORIGINAL_APPSECRET = "3aec5a64f46ee29fbe7ed2b3ac1aea36";
	public static final String PARTNER = "1485136602";	
	public static final String PARTNERKEY = "014672e0f9fc69bbfd89a5e28a3cced5";	
	public static final String WECHAT_TOKEN = "qingpu";
	
	//获取用户基本信息时获取access_token和openid
	public static final String ACCESSTOKEN_OPENID = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
	
	public static final byte HEADER_BYTE = 0x7e;
	public static final byte ENCRYPT_BY_NONE = 0x00;
	
	// 树莓派发送的数据类型
	public static final byte RASTBERRY_REG = 0x10; // 注册
	public static final byte RASTBERRY_HEART_BEAT = 0x11; // 收到树莓派心跳
	public static final byte REPLY_RASTBERRY_HEART_BEAT = 0x12; // 回复树莓派心跳命令
	
	// 收到关于终端的消息
	public static final byte END_REGISTER_TO_SERVER = 0x30; // 终端使用按键Key1发送注册信息到数据库
	public static final byte END_REPLY_RECVED_COIN = 0x31; // 终端回复收到投币命令
	
	// 业务相关命令
	public static final byte TO_PAY_COIN = 0x50; // 线上进行投币操作
	public static final byte ENDDEVICE_REPLY_RECVED_COIN = (byte) 0x82; // 终端设备回复的收到金币命令
	public static final byte ENDDEVICE_REPORT_GAME_REWARD = (byte) 0x83; // 终端设备返回的奖励彩票个数	
}
