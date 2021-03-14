package com.xlmkit.springboot.action;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;

import com.xlmkit.springboot.action.sdk.XJson;
import com.xlmkit.springboot.action.util.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
@Component
public class ActionArgumentResolver implements HandlerMethodArgumentResolver {

	private @Autowired ValidatorFactory validatorFactory;
	private @Autowired ActionService actionService;
	private @Autowired(required = false) XJsonValidator xJsonValidator;
	private ParserConfig parserConfig = new ParserConfig(true);
	private Feature[] features = new Feature[] { //
			Feature.InitStringFieldAsEmpty, //
			Feature.NonStringKeyAsString };

	public ParserConfig getParserConfig() {
		return parserConfig;
	}

	public Feature[] getFeatures() {
		return features;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return actionService.getMethods().contains(parameter.getMethod());
	}

	public <T> String _validate(T object) {
		if (object == null) {
			return null;
		}
		Set<ConstraintViolation<Object>> result = validatorFactory.getValidator().validate(object);
		if (result.size() == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (ConstraintViolation<Object> item : result) {
			sb.append(",");
			sb.append(item.getMessage() + "[" + item.getPropertyPath() + "]");
		}
		return sb.substring(1);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
		XJson xJson = HttpUtils.getXJson(request);
		if (xJson == null) {
			throw new RuntimeException("数据格式错误");
		}
		if (xJsonValidator != null) {
			xJsonValidator.validate(xJson,parameter);
		}
		return parseAndValidate(parameter.getParameterType(), xJson.getJsonContent());
	}

	public Object parseAndValidate(Class<?> type, String body) {
		Object bodyObject = JSON.parseObject(body, type, parserConfig, features);
		String validateResult = null;
		if ((validateResult = _validate(bodyObject)) != null) {
			throw new IllegalArgumentException(validateResult);
		}
		return bodyObject;
	}

	public <T> T parseNoValidate(Class<T> type, String body) {
		return JSON.parseObject(body, type, parserConfig, features);
	}

}
