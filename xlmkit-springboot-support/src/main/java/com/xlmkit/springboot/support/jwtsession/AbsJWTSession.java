package com.xlmkit.springboot.support.jwtsession;

import com.alibaba.fastjson.JSONObject;

public abstract class AbsJWTSession {
	public void init(String id) {
	}

	public void init(JSONObject jsonBody, String id, String data) {
		init(id, data);
		init(id);
	}

	public void init(String id, String data) {
		init(id);
	}

	public String getSecret(String id) {
		return null;
	}

}
