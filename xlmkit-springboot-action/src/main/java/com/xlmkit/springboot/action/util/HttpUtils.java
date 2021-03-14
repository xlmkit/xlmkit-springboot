package com.xlmkit.springboot.action.util;

import com.xlmkit.springboot.action.sdk.XJson;
import com.xlmkit.springboot.action.sdk.XJsonException;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;



public class HttpUtils {

	public static XJson getXJson(HttpServletRequest request) {
		XJson body = (XJson) request.getAttribute(XJson.class.getName());
		if (body != null) {
			return body;
		}
		try {

			String content = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
			body = XJson.parseXJson(content);
			request.setAttribute(XJson.class.getName(), body);
			return body;
		} catch (XJsonException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
