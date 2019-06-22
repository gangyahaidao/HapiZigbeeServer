package com.qingpu.hapihero.device.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * 树莓派节点
 * */
@Entity
@Table(name="rastberry_device")
@Data
public class RastberryDevice {
	
	@Id
	@GeneratedValue
	private Long id;
	
	private String machineId; // 设备编号
	private	Date date;
}
