package com.qingpu.hapihero.device.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.qingpu.hapihero.device.entity.CoordinatorDevice;

public interface ICoordinatorDeviceDao extends JpaRepository<CoordinatorDevice, Long> {

}
