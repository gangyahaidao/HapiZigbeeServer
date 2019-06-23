package com.qingpu.hapihero.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.qingpu.hapihero.common.utils.BackToWeiXin;
import com.qingpu.hapihero.common.utils.CommonUtils;
import com.qingpu.hapihero.common.utils.EmojiUtil;
import com.qingpu.hapihero.common.utils.GetWxOrderno;
import com.qingpu.hapihero.common.utils.MessageUtil;
import com.qingpu.hapihero.common.utils.PayNotifyData;
import com.qingpu.hapihero.common.utils.QingpuConstants;
import com.qingpu.hapihero.common.utils.RequestHandler;
import com.qingpu.hapihero.common.utils.Sha1Util;
import com.qingpu.hapihero.common.utils.TenpayUtil;
import com.qingpu.hapihero.common.utils.WeiXinUtils;
import com.qingpu.hapihero.socketservice.ClientRastberryDeviceSocket;
import com.qingpu.hapihero.socketservice.ResponseSocketUtils;
import com.qingpu.hapihero.socketservice.ServerSocketThread;
import com.qingpu.hapihero.user.dao.IUserWeixinOriginalDao;
import com.qingpu.hapihero.user.entity.UserWeixinOriginal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Api(tags = "UserWeixinController", description = "用于获取微信公众号用户的信息接口")
@Controller
@RequestMapping("/userweixin")
public class UserWeixinController extends HandlerInterceptorAdapter {
    @Autowired
    IUserWeixinOriginalDao userWeixinOriginalDao;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @ApiOperation("手机扫码测试投币动作")
    @RequestMapping("/addCoin")
    public String addCoin() {
    	return "addCoin";
    }
    
    @ResponseBody
    @RequestMapping("/startGame")
    public String startGame(@RequestBody String body) {
    	JSONObject retJsonObj = new JSONObject();
    	retJsonObj.put("code", 0);
    	
    	if(body != null && body != ""){
			JSONObject jsonObj = new JSONObject(body);
			int globalCoinNum = jsonObj.getInt("globalCoinNum"); 
			logger.debug("@globalCoinNum = " + globalCoinNum);
			synchronized (ServerSocketThread.rastberryMachineMap) {
				ClientRastberryDeviceSocket deviceSocket = ServerSocketThread.rastberryMachineMap.get("1");
				if(deviceSocket != null) {
					byte[] content = new byte[2];
					content[0] = 0x01; // 设备编号
					content[1] = (byte) globalCoinNum;
					ResponseSocketUtils.sendBytesToSocket(content, deviceSocket.getClient(), QingpuConstants.TO_PAY_COIN, QingpuConstants.ENCRYPT_BY_NONE);					
				} else {
					retJsonObj.put("code", -1);
				}				
			}					
    	}
    	    	    	
    	logger.debug("@retJsonObj.toString() = " + retJsonObj.toString());
    	return retJsonObj.toString();
    }
    

    @ApiOperation("获取扫码用户详细信息接口1")
    @RequestMapping("/getUserInfoStep1")
    public void getUserInfoStep1(HttpServletRequest request, HttpServletResponse response){
        String orderId = request.getParameter("orderId"); // 获取二维码携带的订单id
        String machineId = request.getParameter("machineId"); // 页面websocket编号，用于通知页面改变现实内容
        String appid = QingpuConstants.ORIGINAL_APPID;
        String backUri = "http://qp.qssrm.com/FxiedRobotSys/userweixin/getUserInfoStep2"+"?machineId="+machineId+"&orderId="+orderId;
        try {
            backUri = URLEncoder.encode(backUri, "UTF-8");
            StringBuilder builder = new StringBuilder();
            builder.append("https://open.weixin.qq.com/connect/oauth2/authorize?")
                    .append("appid=" + appid)
                    .append("&redirect_uri=")
                    .append(backUri)
                    .append("&response_type=code&scope=snsapi_base&state=123#wechat_redirect");
            response.sendRedirect(builder.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ApiOperation("获取用户详细信息的回调接口2")
    @RequestMapping("/getUserInfoStep2")
    public String getUserInfoStep2(HttpServletRequest request, HttpServletResponse response, Model model) throws ServletException, IOException {
        String code = request.getParameter("code");//微信返回的code
        logger.debug("@@获取Original微信openid微信服务器返回code = " + code);
        if(code == null){
            logger.debug("@@获取用户Openid失败");
            return null;
        }
        String orderId = request.getParameter("orderId"); // 获取二维码携带的订单id
        String machineId = request.getParameter("machineId"); // 页面websocket编号
        String appid = QingpuConstants.ORIGINAL_APPID;
        String appsecret = QingpuConstants.ORIGINAL_APPSECRET;
        //使用code换取access_token、openid等，access_token+openid可以用来换取用户个人详细信息
        Map<String, String> map1 = WeiXinUtils.getAccessTokenAndOpenid(code);
        logger.debug("@@获取original_openid返回对象 = " + map1);
        String openId = map1.get("openid");
        String access_token = map1.get("access_token");
        //使用access_token和openid获取用户基本信息
        String URL_getUserInfo = "https://api.weixin.qq.com/sns/userinfo?access_token="+access_token+"&openid="+openId+"&lang=zh_CN";
        Map<String, String> map = CommonUtils.httpsRequest(URL_getUserInfo, "GET", null);

        //如果不能成功获取用户个人信息
        if(map.get("errcode") != null && map.get("errmsg") != null){
            //说明未关注公众号,使用snsapi_userinfo重新请求
            String backUrl = "http://qp.qssrm.com/FxiedRobotSys/userweixin/getUserInfoStep2"+"?machineId="+machineId+"&orderId="+orderId;
            backUrl = URLEncoder.encode(backUrl, "UTF-8");
            StringBuilder builder = new StringBuilder();
            builder.append("https://open.weixin.qq.com/connect/oauth2/authorize?")
                    .append("appid=" + appid)
                    .append("&redirect_uri=")
                    .append(backUrl)
                    .append("&response_type=code&scope=snsapi_base&state=123#wechat_redirect");
            response.sendRedirect(builder.toString());
            return null;
        }

        String openid = map.get("openid");
        UserWeixinOriginal userWX = userWeixinOriginalDao.findByOpenid(openid);
        if(userWX != null){
            //更新用户信息
            logger.debug("@@Original微信更新用户信息 nickname = " + map.get("nickname"));
            userWX.setCity(map.get("city"));
            userWX.setHeadimageurl(map.get("headimgurl"));
            userWX.setNickname(EmojiUtil.emojiConvert(map.get("nickname"))); // 将昵称进行转换
            userWX.setOpenid(openid);
            userWX.setProvince(map.get("province"));
            userWX.setSex(map.get("gender"));
            userWeixinOriginalDao.save(userWX);
        }else {
            logger.debug("@@Original微信新增扫码用户 nickname = " + map.get("nickname"));
            userWX = new UserWeixinOriginal(openid,
                    EmojiUtil.emojiConvert(map.get("nickname")),
                    map.get("headimgurl"),
                    map.get("gender"),
                    map.get("province"),
                    map.get("city"),
                    new Date()
                    );
            userWeixinOriginalDao.save(userWX);
        }

        // 跳转到准备支付页面
        return "scan_topay";
    }

    @ApiOperation("用户付款按钮请求接口，进行微信付款1")
    @RequestMapping("/weixinPayRequest")
    public void weixinPayRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Double totalMoney = Double.valueOf(request.getParameter("totalMoney"));
        String total_fee = (int)(totalMoney*100)+"";
        String orderId = request.getParameter("orderId");
        String openid = request.getParameter("openid");
        String params = total_fee+";"+orderId+";"+openid;
        String appid = QingpuConstants.ORIGINAL_APPID;
        String backUri = "http://qp.qssrm.com/FxiedRobotSys/userweixin/weixinPayRequest2"+"?params="+params;
        StringBuilder build = new StringBuilder();
        build.append("https://open.weixin.qq.com/connect/oauth2/authorize?");
        build.append("appid=" + appid);
        build.append("&redirect_uri=");
        build.append(backUri);
        build.append("&response_type=code&scope=snsapi_base&state=123#wechat_redirect");
        response.sendRedirect(build.toString());
    }

    @ApiOperation("生成用户付款订单")
    @RequestMapping("weixinPayRequest2")
    public String weixinPayRequest2(HttpServletRequest request, HttpServletResponse response, Model model) {
        String code = request.getParameter("code");//微信返回的code
        if(code == null){
            return "pay_error";
        }
        String[] splitArr = request.getParameter("params").split(";");
        String total_fee = splitArr[0];
        String orderId = splitArr[1];
        String openid = splitArr[2];
        String partner = QingpuConstants.PARTNER; //商户相关信息
        String partnerkey = QingpuConstants.PARTNERKEY;
        String currTime = TenpayUtil.getCurrTime();
        String strTime = currTime.substring(8, currTime.length());
        String strRandom = TenpayUtil.buildRandom(4) + "";
        String strReq = strTime + strRandom;
        String mch_id = partner;
        String nonce_str = strReq;
        // 商品描述根据情况修改
        String body = "G58商场哈皮英雄游戏机消费";
        String attach = orderId; // 商家附加数据，此数据会在支付结果通知信息中原样返回
        String out_trade_no = orderId.substring(0, 8) + "_" + new Date().getTime(); // 防止多次使用同一个订单号测试，微信支付生成订单时返回"商户订单号重复"错误
        String spbill_create_ip = request.getRemoteAddr();

        String notify_url = "http://qp.qssrm.com/FxiedRobotSys/userweixin/notifyURL"; //这里notify_url是 支付完成后微信发给该链接信息，可以判断会员是否支付成功，改变订单状态等。
        String trade_type = "JSAPI";
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", QingpuConstants.ORIGINAL_APPID);
        packageParams.put("mch_id", mch_id);
        packageParams.put("nonce_str", nonce_str);
        packageParams.put("body", body);
        packageParams.put("attach", attach);
        packageParams.put("out_trade_no", out_trade_no);
        packageParams.put("total_fee", total_fee);
        packageParams.put("spbill_create_ip", spbill_create_ip);
        packageParams.put("notify_url", notify_url);
        packageParams.put("trade_type", trade_type);
        packageParams.put("openid", openid);
        RequestHandler reqHandler = new RequestHandler(request, response);
        reqHandler.init(QingpuConstants.ORIGINAL_APPID, QingpuConstants.ORIGINAL_APPSECRET, partnerkey);
        String sign = reqHandler.createSign(packageParams);
        String xml = "<xml>"
                + "<appid>" + QingpuConstants.ORIGINAL_APPID + "</appid>"
                + "<mch_id>" + mch_id + "</mch_id>"
                + "<nonce_str>" + nonce_str+ "</nonce_str>"
                + "<sign>" + sign + "</sign>"
                + "<body><![CDATA[" + body + "]]></body>"
                + "<attach>" + attach + "</attach>"
                + "<out_trade_no>" + out_trade_no + "</out_trade_no>"
                + "<total_fee>"+ total_fee + "</total_fee>"
                + "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>"
                + "<notify_url>" + notify_url + "</notify_url>"
                + "<trade_type>" + trade_type + "</trade_type>"
                + "<openid>" + openid + "</openid>"
                +"</xml>";
        String allParameters = "";
        try {
            allParameters = reqHandler.genPackage(packageParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String createOrderURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        String prepay_id = "";
        try {
            new GetWxOrderno();
            prepay_id = GetWxOrderno.getPayNo(createOrderURL, xml);
            System.out.println("@@prepay_id = " + prepay_id);
            if (prepay_id.equals("")) {
                return "pay_error";
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        SortedMap<String, String> finalpackage = new TreeMap<String, String>();
        String appid2 = QingpuConstants.ORIGINAL_APPID;
        String timestamp = Sha1Util.getTimeStamp();
        String nonceStr2 = nonce_str;
        String prepay_id2 = "prepay_id=" + prepay_id;
        String packages = prepay_id2;
        finalpackage.put("appId", appid2);
        finalpackage.put("timeStamp", timestamp);
        finalpackage.put("nonceStr", nonceStr2);
        finalpackage.put("package", packages);
        finalpackage.put("signType", "MD5");
        String finalsign = reqHandler.createSign(finalpackage);

        model.addAttribute("appid", appid2);
        model.addAttribute("timeStamp", timestamp);
        model.addAttribute("nonceStr", nonceStr2);
        model.addAttribute("package", packages);
        model.addAttribute("sign", finalsign);

        return "pay"; // 跳转到输密码付款页面
    }

    @ApiOperation("用户支付成功之后微信服务器的回调接口")
    @RequestMapping("notifyURL")
    public synchronized void notifyURL(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("@@Recv notifyURL()");
        Map<String, String> map = MessageUtil.parseXML(request);
        PayNotifyData weChatBean = new PayNotifyData();
        weChatBean.setAppid(map.get("appid"));
        weChatBean.setAttach(map.get("attach"));
        weChatBean.setBank_type(map.get("bank_type"));
        weChatBean.setCash_fee(map.get("cash_fee"));
        weChatBean.setFee_type(map.get("fee_type"));
        weChatBean.setIs_subscribe(map.get("is_subscribe"));
        weChatBean.setMch_id(map.get("mch_id"));
        weChatBean.setNonce_str(map.get("nonce_str"));
        weChatBean.setOpenid(map.get("openid"));
        weChatBean.setOut_trade_no(map.get("out_trade_no"));
        weChatBean.setResult_code(map.get("result_code"));
        weChatBean.setReturn_code(map.get("return_code"));
        weChatBean.setSign(map.get("sign"));
        weChatBean.setTime_end(map.get("time_end"));
        weChatBean.setTotal_fee(map.get("total_fee"));
        weChatBean.setTrade_type(map.get("trade_type"));
        weChatBean.setTransaction_id(map.get("transaction_id"));

        PrintWriter writer = response.getWriter();
        BackToWeiXin backToWeiXin = new BackToWeiXin();
        //判断确实支付成功,同时验证sign数据，确保没有被篡改过
        if(weChatBean.calculateSign().equals(weChatBean.getSign())
                &&"SUCCESS".equals(map.get("return_code"))
                && "SUCCESS".equals(map.get("result_code"))) {
            backToWeiXin.setReturn_code("SUCCESS");
            backToWeiXin.setReturn_msg("OK");
            MessageUtil.xstream.alias("xml", backToWeiXin.getClass());
            String backstr = MessageUtil.xstream.toXML(backToWeiXin).replaceAll("__", "_");
            //多次通知微信服务器已收到付款成功通知
            for(int i = 0; i < 6; i++){
                writer.write(backstr);
                writer.flush();
            }
            String orderId = URLDecoder.decode(map.get("attach"), "utf-8"); // 获取传递过来的额外数据，订单UUID
            
            // ????????????????????????????????????????向树莓派发送进行投币消息
            
        } else {
            logger.error("微信回调支付失败，返回信息 = " + map);
            backToWeiXin.setReturn_code("FAIL");
            backToWeiXin.setReturn_msg("fail to pay");
            MessageUtil.xstream.alias("xml", backToWeiXin.getClass());
            String noticeStr = MessageUtil.xstream.toXML(backToWeiXin).replaceAll("__", "_");
            writer.write(noticeStr);
            writer.flush();
        }
    }
}
