package com.qingpu.hapihero.common.utils;

/**
 * 数据处理相关的工具函数
 * */
public class DataProcessUtils {
	/**
	 * 合并传递进来的多个数组
	 * */
	public static byte[] mergeArray(byte[]... a) {
		// TODO Auto-generated method stub
		// 合并完之后数组的总长度  
        int index = 0;  
        int sum = 0;  
        for (int i = 0; i < a.length; i++) {  
            sum = sum + a[i].length;  
        }  
        byte[] result = new byte[sum];  
        for (int i = 0; i < a.length; i++) {  
            int lengthOne = a[i].length;  
            if(lengthOne==0){  
                continue;  
            }  
            // 拷贝数组  
            System.arraycopy(a[i], 0, result, index, lengthOne);  
            index = index + lengthOne;  
        }  
        return result;
	}
	/**
	 * 向数组中添加一个字节
	 * */
	public static byte[] appendByte(byte[] a, byte b) {
		int length = a.length;
		byte[] resultBytes = new byte[length + 1];
		System.arraycopy(a, 0, resultBytes, 0, length);
		resultBytes[length] = b;
		return resultBytes;
	}
	/**
	 * 获取字节数组头部的若干大小字节
	 * */
	public static byte[] getFrontBytes(byte[] source, int frontNum) {
		byte[] frontBytes = new byte[frontNum];
		System.arraycopy(source, 0, frontBytes, 0, frontNum);
		return frontBytes;
	}
	/**
	 * 获取字节数组尾部的字节
	 * */
	public static byte[] getAfterBytes(byte[] source, int afterNum) {
		int length = source.length;
		byte[] afterBytes = new byte[afterNum];
		System.arraycopy(source, length - afterNum, afterBytes, 0, afterNum);
		return afterBytes;
	}
	/**
	 * 移除字节数组的前若干字节
	 * */
	public static byte[] removeFrontBytes(int frontNum, byte[] source) {
		return copyByte(frontNum, source.length - frontNum, source);
	}
	public static byte[] copyByte(int start, int length, byte[] source) {
		byte[] des = new byte[length];
		System.arraycopy(source, start, des, 0, length);
		return des;
	}
	/**
	 * 将int转换为byte数组
	 * @param int
	 * @param 需要转换为数组的位数
	 * */
	public static byte[] toByteArray(int iSource, int iArrayLen) {
	    byte[] bLocalArr = new byte[iArrayLen];
	    for (int i = 0; (i < 4) && (i < iArrayLen); i++) {
	        bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
	    }
	    return bLocalArr;
	}
	/**
	 * 将字节数组转换为十六进制字符串输出
	 * */
	public static String bytesToHexString(byte[] bytes){
		StringBuilder hexResult = new StringBuilder("");
		if(bytes == null || bytes.length == 0)
			return null;
		for(int i = 0; i < bytes.length; i++){
			String hex = Integer.toHexString(bytes[i]&0xff);
			if(hex.length() < 2){
				hexResult.append('0');
			}
			hexResult.append(hex + "-");
		}
		
		return hexResult.toString();
		
		/**
		 * 另一种解析方式
		Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;        
		 * */
	}
	/**
	 * 替换数据特殊字节
	 * */
	public static byte[] replaceData(byte[] data) {
		byte[] t = new byte[0];
		int total = data.length;//记录转换之后的字节数组长度
		
		for(int i = 0; i < data.length-1; i++){						
			if((data[i] == (byte)0x7d) && (data[i+1] == (byte)0x02)){				
				t = appendByte(t, (byte)0x7e);
				i++;				
			}else if(data[i] == (byte)0x7d && data[i+1] == (byte)0x01){				
				t = appendByte(t, (byte)0x7d);
				i++;				
			}else{
				t = appendByte(t, data[i]);
			}
		}
		t = appendByte(t, data[total-1]);
		
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
