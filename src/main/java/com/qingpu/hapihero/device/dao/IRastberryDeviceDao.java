package com.qingpu.hapihero.device.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.qingpu.hapihero.device.entity.RastberryDevice;

public interface IRastberryDeviceDao extends JpaRepository<RastberryDevice, Long> {

}
