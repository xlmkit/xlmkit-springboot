package com.xlmkit.springboot.action.sdk;

import java.security.MessageDigest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import lombok.Data;

@Data
public class XJson {
	private ParserConfig parserConfig = new ParserConfig(true);
	public static SerializerFeature[] serializerFeatures = new SerializerFeature[] { //
			// SerializerFeature.PrettyFormat, //
			SerializerFeature.WriteDateUseDateFormat, //
			SerializerFeature.SortField, //
			SerializerFeature.MapSortField, //
			SerializerFeature.DisableCircularReferenceDetect };
	private String sign;
	private String jsonContent;
	private JSONObject jsonObject;
	private String access_sign_type;
	private String access_timestamp;
	private String access_key_id;
	private String access_secret;

	public XJson(String sign, String jsonContent) {
		super();
		this.sign = sign;
		this.jsonContent = jsonContent;
		this.jsonObject = JSONObject.parseObject(jsonContent);
		this.access_sign_type = jsonObject.getString("access_sign_type");
		this.access_timestamp = jsonObject.getString("access_timestamp");
		this.access_key_id = jsonObject.getString("access_key_id");
	}

	public XJson() {

	}

	public boolean validate(String secret) {
		this.access_secret = secret;
		return MD5_32bit(secret + jsonContent).equals(sign);
	}

	public static final String MD5_32bit(String readyEncryptStr) {
		try {
			if (readyEncryptStr != null) {
				// Get MD5 digest algorithm's MessageDigest's instance.
				MessageDigest md = MessageDigest.getInstance("MD5");
				// Use specified byte update digest.
				md.update(readyEncryptStr.getBytes());
				// Get cipher text
				byte[] b = md.digest();
				// The cipher text converted to hexadecimal string
				StringBuilder su = new StringBuilder();
				// byte array switch hexadecimal number.
				for (int offset = 0, bLen = b.length; offset < bLen; offset++) {
					String haxHex = Integer.toHexString(b[offset] & 0xFF);
					if (haxHex.length() < 2) {
						su.append("0");
					}
					su.append(haxHex);
				}
				return su.toString();
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}

	}
	public static XJson parseXJson(String xjson) throws XJsonException {
		try {
			int a = xjson.indexOf('{');
			String sign = "";
			String  content = xjson;
			if(a>0) {
				sign = xjson.substring(0, a);
				content = xjson.substring(a);
			}
			return new XJson(sign, content);
		} catch (Exception e) {
			throw new XJsonException();
		}

	}
	public <T> T toObject(Class<T> returnType) {
		return JSONObject.parseObject(jsonContent, returnType, parserConfig);
	}

	public static String toXString(Object object, Client client) {
		JSONObject json  = (JSONObject) JSON.toJSON(object);
		json.put("access_sign_type", client.getAccess_sign_type());
		json.put("access_timestamp", System.currentTimeMillis() + "");
		json.put("access_key_id", client.getAccess_key_id());
		String jsonStr = JSON.toJSONString(json, serializerFeatures);
		String sign = XJson.MD5_32bit(client.getAccess_key_secret() + jsonStr);
		return sign + jsonStr;
	}
}
