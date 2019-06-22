package com.qingpu.hapihero.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiUtil {

	
	 /**
	* @Description 将字符串中的emoji表情转换成可以在utf-8字符集数据库中保存的格式（表情占4个字节，需要utf8mb4字符集）
	* @param str
	* 待转换字符串
	* @return 转换后字符串
	* @throws UnsupportedEncodingException
	* exception
	*/
	public static String emojiConvert(String str){
		StringBuffer sb = new StringBuffer();
		try{
			//[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]
			String patternString = "([\\x{10000}-\\x{10ffff}\ud800-\udfff])";
			
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(str);
			
			while(matcher.find()) {
				try {
					matcher.appendReplacement(
					sb,
					"[[" + URLEncoder.encode(matcher.group(1), "UTF-8") + "]]");
				} catch(UnsupportedEncodingException e) {
					throw e;
				}
			}
			matcher.appendTail(sb);
		}catch(Exception e){
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	* @Description 还原utf8数据库中保存的含转换后emoji表情的字符串
	* @param str
	* 转换后的字符串
	* @return 转换前的字符串
	* @throws UnsupportedEncodingException
	* exception
	*/
	public static String emojiRecovery(String str) {
		StringBuffer sb = new StringBuffer();
		try{
			String patternString = "\\[\\[(.*?)\\]\\]";
			
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(str);
		
			
			while(matcher.find()) {
				try {
					matcher.appendReplacement(sb, URLDecoder.decode(matcher.group(1), "UTF-8"));
				} catch(UnsupportedEncodingException e) {
				//LOG.error("emojiRecovery error", e);
				throw e;
				}
			}
			matcher.appendTail(sb);
		}catch(Exception e){
			e.printStackTrace();
		}
			
		//LOG.debug("emojiRecovery " + str + " to " + sb.toString());
		return sb.toString();
	}
}
