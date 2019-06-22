package com.qingpu.hapihero.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.qingpu.hapihero.user.entity.UserWeixinOriginal;

public interface IUserWeixinOriginalDao extends JpaRepository<UserWeixinOriginal, Long> {
	
	/**
	 * 根据用户openid查找信息
	 * */
	public UserWeixinOriginal findByOpenid(String openid);
}
