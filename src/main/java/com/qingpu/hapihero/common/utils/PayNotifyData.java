package com.qingpu.hapihero.common.utils;

import lombok.Data;

@Data
public class PayNotifyData {
    private String appid;
    private String attach;
	private String bank_type;
    private String cash_fee;
    private String fee_type;
    private String is_subscribe;
    private String mch_id;
    private String nonce_str;
    private String openid;
    private String out_trade_no;
    private String result_code;
    private String return_code;
    private String sign;
    private String time_end;
    private String total_fee;
    private String trade_type;
    private String transaction_id;

	public String calculateSign(){
        String str = "appid=" + appid +
        		"&attach=" + attach +
                "&bank_type=" + bank_type +
                "&cash_fee=" + cash_fee +
                "&fee_type=" + fee_type+
                "&is_subscribe=" + is_subscribe +
                "&mch_id=" + mch_id +
                "&nonce_str=" + nonce_str +
                "&openid=" + openid +
                "&out_trade_no=" + out_trade_no +
                "&result_code=" + result_code +
                "&return_code=" + return_code +
                "&time_end=" + time_end +
                "&total_fee=" + total_fee +
                "&trade_type=" + trade_type +
                "&transaction_id=" + transaction_id +
                "&key=" + QingpuConstants.PARTNERKEY;
        System.out.println("@@进行签名验证的字符串 = " + str);
        return MD5Util.MD5Encode(str, "UTF-8").toUpperCase();
    }
}
