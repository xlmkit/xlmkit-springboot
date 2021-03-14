package com.xlmkit.springboot.action.sdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import com.xlmkit.springboot.action.SDKException;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@SuppressWarnings("unchecked")
@Slf4j
public class Client implements InvocationHandler {
	private Map<Class<?>, Object> actionMap = new HashMap<>();
	private String access_sign_type = "MD5";
	private String access_key_id;
	private String access_key_secret;
	private String basepath;
	private int timeout = 5000;
	private int connectionTimeout = 5000;

	public HttpResponse send(HttpRequest request) {
		return request.send();
	}

	public Client(String basepath, String access_key_id, String access_key_secret) {
		super();
		this.basepath = basepath;
		this.access_key_id = access_key_id;
		this.access_key_secret = access_key_secret;
	}

	public <T> T getAction(Class<T> class1) {
		return (T) actionMap.getOrDefault(class1, createAction(class1));
	}

	private <T> T createAction(Class<T> class1) {
		Object action = Proxy.newProxyInstance(Client.class.getClassLoader(), new Class[] { class1 }, this);
		actionMap.put(class1, action);
		return (T) action;
	}

	public String sendAndObtainStringContent(HttpRequest request, Object reqBody) throws SDKException {
		JSONObject object = (JSONObject) JSONObject.toJSON(reqBody);

		String body = XJson.toXString(object, this);
		request.body(body.getBytes(StandardCharsets.UTF_8), "application/x-json");
		HttpResponse response = null;
		try {
			response = send(request);
		} catch (Exception e) {
			log.error("请求异常1,msg={},url={},body={}",e.getMessage(), request.url(), body);
			throw new SDKException("LOCAL_INVODE_ERROR", "网络错误【1】" + e.getMessage());
		}
		if (response.statusCode() != 200) {
			log.error("请求异常状态错误code={},url={},body={}", response.statusCode(), request.url(), body);
			throw new SDKException("LOCAL_NO_200", "请求错误【2】");
		}
		String resultBody = new String(response.bodyBytes(), StandardCharsets.UTF_8);
		if (resultBody.startsWith("{")) {
			JSONObject jsonObject = JSONObject.parseObject(resultBody);
			log.error("请求数据错误,url={},body={}", request.url(), body);
			log.error("请求数据错误,resultBody={}", resultBody);
			throw new SDKException("REQ_ERROR", jsonObject.getString("return_msg"));
		}
		return resultBody;
	}

	public <T> T send(HttpRequest request, Object reqBody, Class<T> returnType) throws SDKException {
		String resultBody = null;
		try {
			resultBody = sendAndObtainStringContent(request, reqBody);
			XJson xJson = XJson.parseXJson(resultBody);
			if (!xJson.validate(access_key_secret)) {
				log.error("请求错误，验签不通过,url={}", request.url());
				throw new SDKException("LOCAL_PARSE_ERROR", "解析异常【3】,data=" + resultBody);
			}
			return xJson.toObject(returnType);
		} catch (Exception e) {
			if (e instanceof SDKException) {
				throw (SDKException) e;
			}
			log.error("请求异常2,url={}", request.url(), e);
			throw new SDKException("LOCAL_PARSE_ERROR", "解析异常【4】,data=" + resultBody);
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String path = "/" + method.getDeclaringClass().getSimpleName().replaceAll("Action", "") + "/"
				+ method.getName();
		HttpRequest request = HttpRequest.post(basepath + path);
		request.timeout(timeout);
		request.connectionTimeout(connectionTimeout);
		Object body = args[0].getClass().isAssignableFrom(Client.class) ? args[1] : args[0];
		return send(request, body, method.getReturnType());

	}

}