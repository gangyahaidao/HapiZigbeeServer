package com.qingpu.hapihero.device.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.qingpu.hapihero.device.entity.EndDevice;

public interface IEndDeviceDao extends JpaRepository<EndDevice, Long> {
	
	/**
	 * 根据设备编号查找
	 * */
	public EndDevice findByControlNum(Byte controlNum);
}
