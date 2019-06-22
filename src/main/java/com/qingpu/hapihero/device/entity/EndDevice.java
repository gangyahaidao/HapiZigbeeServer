package com.qingpu.hapihero.device.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * 封装zigbee终端节点设备
 * */
@Entity
@Table(name="enddevice")
@Data
public class EndDevice {
	@Id
	@GeneratedValue
	private Long id;
	
	private Byte 		controlNum;  // 设备的编号，每个设备在烧录程序时指定的编号，用于数据通信
	private String 		pointName; // 节点命名，文字描述
	private Boolean 	online;   // 节点是否在线，节点连接入网时会发送上线消息，之后如果被协调器检测到掉线，协调器会发送掉线命令，重新入网会发送上线命令
	private Long 		needCoinToStart; // 设备对应的游戏机开始游戏需要的游戏币个数
	private Integer		rewardMinScore; // 玩家玩游戏获取奖励的最低分数
	private Date date;
}
