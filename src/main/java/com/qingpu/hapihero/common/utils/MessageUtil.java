/**
* Title: MessageUtil.java
* Description: 
* Copyright: Copyright (c) 2016
* Company: Biceng
* @date 2017-3-7
* @version 1.0
*/
package com.qingpu.hapihero.common.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.AbstractJsonWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.*;

/**
 * 消息工具类
 * @author wang_gang
 *
 */
public class MessageUtil {	
	/**
	 * 扩展stream，使其支持CDATA块
	 * */
	public static XStream xstream = new XStream(new XppDriver(){
		@Override
		public HierarchicalStreamWriter createWriter(Writer out){
			return new PrettyPrintWriter(out){
				//对所有的xml节点的转换都增加CDATA标记
				boolean cdata = true;
				@Override
				public void startNode(String name, Class clazz){
					super.startNode(name, clazz);
				}
				@Override
				protected void writeText(QuickWriter writer, String text){
					if(cdata){
						writer.write("<![CDATA[");
						writer.write(text);
						writer.write("]]>");
					}else{
						writer.write(text);
					}
				}
			};
		}
	});
	/**
	 * 将javabean转换成json字符串
	 * */
	public static String convertBeanToJsonStr(Object object){
		String jsonStr = "";
		XStream xstream = new XStream(new JsonHierarchicalStreamDriver(){
			@Override
			public HierarchicalStreamWriter createWriter(Writer out) {
				return new JsonWriter(out, AbstractJsonWriter.DROP_ROOT_MODE);
			}
		});
		xstream.alias("xml", Object.class);
		return xstream.toXML(object);		
	}

	/**
	 * json字符串转换为Map
	 * @param jsonStr字符串
	 * @return Map
	 * */
	public static Map<String, Object> convertJsonstr2Map(String jsonStr){
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> maps = null;
		try {
			 
            //将json字符串转成map结合解析出来，并打印(这里以解析成map为例)
            maps = objectMapper.readValue(jsonStr, Map.class);
            Set<String> key = maps.keySet();
            Iterator<String> iter = key.iterator();
            while (iter.hasNext()) {
                String field = iter.next();
                //System.out.println(field + ":" + maps.get(field));
            }
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }	
		
		return maps;
	}
	public static Map<String, String> convertXmlStrToMap(String xml) {
		Map<String, String> map = new HashMap<String, String>();		
		Document doc = null;
		int num = 0;
		try {

			doc = DocumentHelper.parseText(xml);
			Element employees = doc.getRootElement();
			for (Iterator j = employees.elementIterator(); j.hasNext();) {
				Element node = (Element) j.next();
				if (map.size() > 0 && null != map.get(node.getName())) {
					map.put(node.getName() + String.valueOf(num),
							node.getText());
				} else {
					map.put(node.getName(), node.getText());
				}
				num++;
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	/**
	 * 解析微信发来的xml数据
	 * @param request
	 * @return Map<String, String>
	 * @exception Exception	 
	 * */
	public static Map<String, String> parseXML(HttpServletRequest request) throws Exception{
        // 将解析结果存储在HashMap中
        Map<String, String> map = new HashMap<String, String>();
        // 从request中取得输入流
        InputStream inputStream = request.getInputStream();
        // 读取输入流
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        // 得到xml根元素
        Element root = document.getRootElement();
        // 得到根元素的所有子节点
        List<Element> elementList = root.elements();
        // 遍历所有子节点
        for (Element e : elementList)
            map.put(e.getName(), e.getText());
        // 释放资源
        inputStream.close();
        inputStream = null;
        return map;
	}
	
	/**
	 * 替换数据特殊字节
	 * */
	public static byte[] replaceData(byte[] data) {
		byte[] t = new byte[0];
		int total = data.length;//记录转换之后的字节数组长度
		
		for(int i = 0; i < data.length-1; i++){						
			if((data[i] == (byte)0x7d) && (data[i+1] == (byte)0x02)){				
				t = ByteUtils.appendByte(t, (byte)0x7e);
				i++;				
			}else if(data[i] == (byte)0x7d && data[i+1] == (byte)0x01){				
				t = ByteUtils.appendByte(t, (byte)0x7d);
				i++;				
			}else{
				t = ByteUtils.appendByte(t, data[i]);
			}
		}
		t = ByteUtils.appendByte(t, data[total-1]);
		
		return t;
	}
	/**
	 * 检验数据
	 * */
	public static boolean checkMessage(byte[] data) {
		byte receivedCheck = data[data.length-2];
		
		byte checkXOR = data[1];
		
		for (int i = 2; i <data.length-2; i++) {  
			checkXOR ^=data[i];  
	    }
		
		if(checkXOR == receivedCheck)
			return true;
		else 
			return false;
	}
}
