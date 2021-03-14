package com.xlmkit.springboot.action;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;


/**
 * Created by 12614 on 2018/5/11.
 */

public class ResultHandler implements HandlerMethodReturnValueHandler, InitializingBean {
	@Autowired
	private RequestMappingHandlerAdapter adapter;
	private HandlerMethodReturnValueHandler delegate;

	@Override
	public void afterPropertiesSet() {
		List<HandlerMethodReturnValueHandler> returnValueHandlers = adapter.getReturnValueHandlers();
		List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(returnValueHandlers);
		decorateHandlers(handlers);
		adapter.setReturnValueHandlers(handlers);
	}

	private void decorateHandlers(List<HandlerMethodReturnValueHandler> handlers) {
		for (int i = 0; i < handlers.size(); i++) {
			HandlerMethodReturnValueHandler handler = handlers.get(i);
			if (handler instanceof RequestResponseBodyMethodProcessor) {
				this.delegate = handler;
				handlers.set(i, this);// 用自定义的OpenApiReturnValueHandler替换掉原来的RequestResponseBodyMethodProcessor类型处理器
				break;
			}
		}
	}

	@Override
	public boolean supportsReturnType(MethodParameter methodParameter) {
		return Result.class.isAssignableFrom(methodParameter.getMethod().getReturnType())
				|| methodParameter.hasParameterAnnotation(ResponseBody.class);
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest) throws Exception {
		delegate.handleReturnValue(returnValue, returnType, mavContainer, webRequest);

	}
}