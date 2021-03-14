package com.xlmkit.springboot.action.sdk;

import com.alibaba.fastjson.JSONObject;

public class ClientResult extends JSONObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ClientResult() {

	}

	public static ClientResult success() {
		ClientResult self = new ClientResult();
		self.put("message", "OK");
		self.put("return_code", "SUCCESS");
		self.put("result_code", "SUCCESS");
		return self;
	}

	public static ClientResult fail(String code, String des) {
		ClientResult self = new ClientResult();

		self.put("return_code", "SUCCESS");
		self.put("result_code", code);
		self.put("result_des", des);
		return self;
	}

	public static ClientResult sysError(String des) {
		ClientResult self = new ClientResult();

		self.put("return_code", "SUCCESS");
		self.put("result_code", "SYSTEM_ERROR");
		self.put("result_des", des);
		return self;
	}

	public String toXString(Client client) {
		return XJson.toXString(this, client);
	}
}
