package com.qingpu.hapihero.user.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * 表示微信公众号用户
 * */
@Entity
@Table(name="user_weixin_original")
@Data
public class UserWeixinOriginal {
	@Id
	@GeneratedValue	
	private Long id;
	
	private String  openid;
	private String  nickname;
	private String  headimageurl;
	private String  sex;
	private String  province;
	private String  city;
	private Date    date;

	public UserWeixinOriginal() {
		// default constructor
	}

	public UserWeixinOriginal(String openid, String nickname, String headimageurl, String sex, String province, String city, Date date) {
		this.openid = openid;
		this.nickname = nickname;
		this.headimageurl = headimageurl;
		this.sex = sex;
		this.province = province;
		this.city = city;
		this.date = date;
	}
}