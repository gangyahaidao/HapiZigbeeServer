package com.qingpu.hapihero.common.utils;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用的数据处理方法
 * */
public class CommonUtils {
	
	/**
	 * 发送异常处理中json数据到客户端
	 * */
	public static void sendExceptionJsonStr(HttpServletResponse response, String jsonStr) {
		try {
			response.setStatus(HttpStatus.OK.value()); //设置状态码
            response.setContentType(MediaType.APPLICATION_JSON_VALUE); //设置ContentType
            response.setCharacterEncoding("UTF-8"); //避免乱码  
            response.setHeader("Cache-Control", "no-cache, must-revalidate");			
            PrintWriter writer = response.getWriter();			
			writer.write(jsonStr);
			writer.flush();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送指定的code和message到客户端
	 * */
	public static void sendJsonStr(HttpServletResponse response, int code, String message){
		JSONObject ret = new JSONObject();
		ret.put("code", code);
		ret.put("message", message);
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json; charset=utf-8");
		try {
			PrintWriter writer = response.getWriter();			
			writer.write(ret.toString());
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 发送json格式字符串到客户端
	 * */	
	public static void sendJsonStr(HttpServletResponse response, String str){
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json; charset=utf-8");
		try {
			PrintWriter writer = response.getWriter();			
			writer.write(str);
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * URL编码（utf-8）
	 * 
	 * @param source
	 * @return
	 */
	public static String urlEncodeUTF8(String source) {
		String result = source;
		try {
			result = java.net.URLEncoder.encode(source, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 根据内容类型判断文件扩展名
	 * 
	 * @param contentType 内容类型
	 * @return
	 */
	public static String getFileExt(String contentType) {
		String fileExt = "";
		if ("image/jpeg".equals(contentType))
			fileExt = ".jpg";
		else if ("audio/mpeg".equals(contentType))
			fileExt = ".mp3";
		else if ("audio/amr".equals(contentType))
			fileExt = ".amr";
		else if ("video/mp4".equals(contentType))
			fileExt = ".mp4";
		else if ("video/mpeg4".equals(contentType))
			fileExt = ".mp4";
		return fileExt;
	}
    
	/**
	 * 从指定的Map中获取value对应的key值，由value获取key
	 * */
	public static Object getMapKey(Map<String, Socket> map, Object value){
		Object key = null;
		
		for(Object getKey:map.keySet()){
			if(map.get(getKey).equals(value)){
				key = getKey;
			}
		}
		
		return key;
	}
	/**
	 * 获取所有Key
	 * */
    public static ArrayList getAllKey(HashMap hm,String value){  
        ArrayList list=new ArrayList();  
        for(Object getKey:hm.keySet()){  
            if (hm.get(getKey).equals(value)) {  
                list.add(getKey);  
            }  
        }  
        return list;  
    }  
    /**
     * @Desc 获取不重复的随机数
     * @param t 选取数据的原数组
     * @param noRepeatNums 从原数组中选取的个数
     * @return 返回选取随机数的数组
     * */
    public static int[] getNoRepeatNumByChange(int[] t, int noRepeatNums) {
    	int[] newArr = new int[noRepeatNums];
        for (int i = 0; i < noRepeatNums; i++) {
            Random random = new Random();
            int s = random.nextInt(t.length - 1) % (t.length - 1 - i + 1)+ i;
            int temp = t[i];
            t[i] = t[s];
            t[s] = temp;
            newArr[i] = t[i];
        }
        return newArr;
    }
    
    /**
     * 方法二：set排异法
     * */    
    public static Set<Integer> getNoRepeatNumBySet(int[] arr, int noRepeatNums){
            Random random = new Random();
            Set<Integer> set = new HashSet<Integer>();
            while (true) {
                int s = random.nextInt(arr.length);
                set.add(Integer.valueOf(arr[s]));
                if (set.size() == noRepeatNums) {
                    break;
                }
            }
            return set;
     }
    /**
     * 产生一个指定范围的随机数  min<= ret <=max
     * */
    public static int getRandomNum(int min, int max){
    	max += 1;
    	Random random = new Random();
    	
    	return random.nextInt(max)%(max-min+1) + min;
    }
    
    /**
     * 判断指定的字符串是否都是数字
     * 匹配返回true
     * */
    public static boolean isNumber(String str) {  
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");  
        return pattern.matcher(str).matches();  
    }

    /**
     * 根据一个随机生成的1-100概率值查找对话概率列表中的下标
     * */
	public static int findProbabilityListIndex(List<Integer> listProb) {
		Random RANDOM = new SecureRandom();
		int num = RANDOM.nextInt(100)+1;
		int totalValue = 0;
		for(int i = 0; i < listProb.size(); i++) {
			int value = listProb.get(i);
			totalValue += value;
			if(totalValue >= num) {
				return i;
			}
		}		
		return 0;
	}
	
	/**
	 * 根据传递进来的小时字符串转换成毫秒数
	 * */
	public static long translateHourStrToMiniSec(String timeStr) {
		long ret = 0;
		if(timeStr.length() > 0) {
			String[] splitArr = timeStr.split(":");
			String hourStr = splitArr[0].replaceAll("^(0+)", ""); // 去除字符串前面为0的部分
			String minitStr = splitArr[1].replaceAll("^(0+)", "");
			if(minitStr.length() <= 0) { // 如果分钟的数值为0整点，则正则之后长度为0
				minitStr = "0";
			}
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8")); // 获取北京时间
			Date date = new Date();
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourStr));
			cal.set(Calendar.MINUTE, Integer.parseInt(minitStr));
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);			
			ret = cal.getTimeInMillis();
		}
		
		return ret;
	}
	
	/**
	 * 获取指定字符串出现的次数
	 * 
	 * @param srcText 源字符串
	 * @param findText 要查找的字符串
	 * @return
	 */
	public static int getAppearCount(String srcText, String findText) {
	    int count = 0;
	    Pattern p = Pattern.compile(findText);
	    Matcher m = p.matcher(srcText);
	    while (m.find()) {
	        count++;
	    }
	    return count;
	}
	
	/**
	 * 将数字字符串转化成汉字，如300000 --》 三十万
	 * */
    public static String transferDigitalToString(String input) {
    	String[] num = {"零","一","二","三","四","五","六","七","八","九","十"};
        String[] unit = {"","十","百","千","万","十","百","千","亿"};
        String[] result;

        String out = "";
        result = new String[input.length()];
        int length = result.length;
        for(int i = 0; i< length; i++) {
            result[i] = String.valueOf(input.charAt(i));
        }
        for(int i = 0; i< length; i++) {
            int back;
            if(!result[i].equals("0")) {
                back = length - i - 1;
                out += num[Integer.parseInt(result[i])];
                out += unit[back];
            } else {
                //最后一位不考虑
                if(i == (length - 1)) {
                    if(length > 4 && result[length - 1].equals("0") && result[length - 2].equals("0") && result[length - 3].equals("0") && result[length - 4].equals("0")){
                        out += unit[4];
                    }
                } else {
                    //九位数，千万，百万，十万，万位都为0，则不加“万”
                    if(length == 9 && result[1].equals("0") && result[2].equals("0") && result[3].equals("0") && result[4].equals("0")) {

                    } else {
                        //大于万位，连着的两个数不为0，万位等于0则加上“万”
                        if(length > 4 && !result[i+1].equals("0") && result[length -5].equals("0")){
                            out += unit[4];
                        }
                    }
                    //万位之后的零显示
                    if(i == length -4 && !result[i+1].equals("0")) {
                        out += num[0];
                    }
                }
            }
        }
        return out;
    }
    
    /**
     * 根据日期输出星期几的字符串
     * */
	public static String dateToWeekDayStr(Date date) {
		String[] weekDays = { "周天", "周一", "周二", "周三", "周四", "周五", "周六"};
		Calendar cal = Calendar.getInstance(); // 获得一个日历
		cal.setTime(date);
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1; // 指示一个星期中的某天。
        return weekDays[w];
	}
	
	/**
	 * 根据日期输出星期几的整型值
	 * */
	public static int dateToWeekDayInt(Date date) {
		Calendar cal = Calendar.getInstance(); // 获得一个日历
		cal.setTime(date);
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1; // 指示一个星期中的某天。 星期天--0 ~ 星期六--6  返回的值比今天要大一天，需要减去1
        if(w == 0) { // 如果是星期天
        	w = 6;
        }else {
        	w = w-1;
        }
		return w;
	}
	
	/**
	 * 将element-ui的日期选择框架输出的字符串转换成Date对象，传递上来的时间字符串少8小时，此函数转换之后可以变成正常的Date对象
	 * */
	public static Date translateDatePickerStrToDate(String timeStr) {
		timeStr = timeStr.replace("Z", " UTC");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
		Date d = null;
		try {
			d = format.parse(timeStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return d;
	}
	
	/**
	 * 去除list中的重复元素
	 * @return 
	 * */
    public static ArrayList<String> getNoRepeatEleList(List<String> arr) {
        ArrayList<String> result = new ArrayList<String>();
        for (Object s : arr) {
            if (Collections.frequency(result, s) < 1)
                result.add((String) s);
        }
        return result;
    }
    
    /**
     * 对Map的value值进行降序排序
     * */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapValueByDescending(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return -(o1.getValue()).compareTo(o2.getValue()); // 降序排序
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapValueByAscending(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return -(o1.getValue()).compareTo(o2.getValue()); // 升序排序
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

	/**
	 * 发送https请求
	 *
	 * @param requestUrl 请求地址
	 * @param requestMethod 请求方式（GET、POST）
	 * @param outputStr 提交的数据
	 * @return map，通过map.get(key)方式获取值)
	 */
	public static Map<String, String> httpsRequest(String requestUrl, String requestMethod, String outputStr) {
		//将解析结果存储在Map中返回
		Map<String, String> map = new HashMap<String, String>();
		JSONObject jsonObject = null;
		InputStream inputStream = null;
		HttpsURLConnection conn = null;
		InputStreamReader inputStreamReader = null;
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new MyX509TrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();

			URL url = new URL(requestUrl);
			conn = (HttpsURLConnection) url.openConnection();
			conn.setSSLSocketFactory(ssf);

			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			conn.setRequestMethod(requestMethod);

			// 当outputStr不为null时向输出流写数据
			if (null != outputStr) {
				OutputStream outputStream = conn.getOutputStream();
				// 注意编码格式
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}

			// 从输入流读取返回内容
			inputStream = conn.getInputStream();
			inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String str = null;
			StringBuffer buffer = new StringBuffer();
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			//将返回的json字符串转化成jsonObject
			jsonObject = JSONObject.fromObject(buffer.toString());
			//将jsonObject转换成map
			XMLSerializer xmlSerializer = new XMLSerializer();
			xmlSerializer.setTypeHintsCompatibility( false );
			String xml = xmlSerializer.write(jsonObject);
			//将xml转化成map
			SAXReader reader = new SAXReader();
			Document document = reader.read(new ByteArrayInputStream(xml.getBytes("utf-8")));
			//得到xml根元素
			Element root = document.getRootElement();
			//得到根元素的所有子节点
			List<Element> elementList = root.elements();
			//遍历所有子节点，将信息存储在Map中
			for(Element e : elementList){
				map.put(e.getName(), e.getText());
			}
		} catch (ConnectException ce) {
		} catch (Exception e) {
		}finally{
			// 释放资源
			try {
				inputStreamReader.close();
				inputStream.close();
				inputStream = null;
				conn.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return map;
	}

	//发送Http get请求，返回接收的字符串
	public static String httpGetStr(String URL){
		HttpGet httpRequst = new HttpGet(URL);
		String result = "";

		try {
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequst);
			if(httpResponse.getStatusLine().getStatusCode() == 200)
			{
				HttpEntity httpEntity = httpResponse.getEntity();
				result = EntityUtils.toString(httpEntity);//取出应答字符串
				result.replaceAll("\r", "");//去掉返回结果中的"\r"字符，否则会在结果字符串后面显示一个小方格
			}
			else{
				httpRequst.abort();
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	//发送http post请求，参数为请求的json字符串
	public static String httpPostJsonStr(String URL, String json) {

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(URL);

		post.setHeader("Content-Type", "application/json");
		post.addHeader("Authorization", "Basic YWRtaW46");
		String result = "";

		try {

			StringEntity s = new StringEntity(json, "utf-8");
			s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
			post.setEntity(s);

			// 发送请求
			HttpResponse httpResponse = client.execute(post);

			// 获取响应输入流
			InputStream inStream = httpResponse.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inStream, "utf-8"));
			StringBuilder strber = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null)
				strber.append(line + "\n");
			inStream.close();

			result = strber.toString();
		} catch (Exception e) {
			System.out.println("request exception");
			throw new RuntimeException(e);
		}

		return result;
	}
}
