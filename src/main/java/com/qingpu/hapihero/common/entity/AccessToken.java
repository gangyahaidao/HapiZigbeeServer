/**
* Title: AccessToken.java
* Description: 
* Copyright: Copyright (c) 2016
* Company: Biceng
* @date 2017-3-8
* @version 1.0
*/
package com.qingpu.hapihero.common.entity;

import lombok.Data;

/**
 * @author wang_gang
 *
 */
@Data
public class AccessToken {
	//获取到的凭证
	private String access_token;
	//凭证的有效时间，单位：秒
	private int expires_in;
}
