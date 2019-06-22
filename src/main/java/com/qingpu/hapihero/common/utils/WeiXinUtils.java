package com.qingpu.hapihero.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 主要是为微信公众号支付功能提供支持
 * @author wang_gang
 *
 */
public class WeiXinUtils {
	//网页auth2.0授权获取用户详细信息使用的Map
	private static Map<String, String> MAP = new HashMap<String, String>();
	private static Date lastTime = null;
	
	/**
	 * auth2.0授权网页获取用户信息，使用code换取access_token、openid等，这是一个access_token的服务器不缓存，不然付款失败，在二维码红包付款，领取红包等地方使用，用于换取用户的详细信息
	 * */
	public synchronized static Map<String, String> getAccessTokenAndOpenid(String code){
		Map<String, String> result = new HashMap<String, String>();
		
		String accessTokenUrl = String.format(QingpuConstants.ACCESSTOKEN_OPENID, QingpuConstants.ORIGINAL_APPID, QingpuConstants.ORIGINAL_APPSECRET, code);
		MAP = CommonUtils.httpsRequest(accessTokenUrl, "GET", null);
		lastTime = new Date();
		result.put("access_token", MAP.get("access_token"));
		result.put("openid", MAP.get("openid"));
		
		return result;
	}
	
	/**
	 * 微信发送数据模板替换函数
	 * */
	public static String render(String template, Map<String, String> data){
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(template);
        
        template = m.replaceAll("");
		
		String regex = "\\{\\{(.+?)\\}\\}";
		if(StringUtils.isBlank(template)){
            return "";
        }
        if(StringUtils.isBlank(regex)){
            return template;
        }
        if(data == null || data.size() == 0){
            return template;
        }
        try {
            StringBuffer sb = new StringBuffer();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(template);
            while (matcher.find()) {
                String name = matcher.group(1);// 键名
                String value = data.get(name);// 键值
                if (value != null){ // 只替换提供值的模板变量
                	matcher.appendReplacement(sb, value);
                }
            }
            matcher.appendTail(sb);
            //去除空格和换行
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return template;
	}	
}
