/**
* Title: MyX509TrustManager.java
* Description: 
* Copyright: Copyright (c) 2016
* Company: Biceng
* @date 2017-3-8
* @version 1.0
*/
package com.qingpu.hapihero.common.utils;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


/**
 * 证书信任管理器，用于https登录
 * @author wang_gang
 *
 */
public class MyX509TrustManager implements X509TrustManager{

	/**
	 * @see X509TrustManager#checkClientTrusted(X509Certificate[], String)
	 */
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see X509TrustManager#checkServerTrusted(X509Certificate[], String)
	 */
	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see X509TrustManager#getAcceptedIssuers()
	 */
	@Override
	public X509Certificate[] getAcceptedIssuers() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
