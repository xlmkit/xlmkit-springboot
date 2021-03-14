package com.xlmkit.springboot.support.asynctask;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

/**
 * @author 小龙码
 * 注册所有用@AsyncTask标记的方法
 */
@Slf4j
public class AsyncTaskBeanPostProcessor implements BeanPostProcessor {

	private Map<String, AsyncTaskInfo> taskInfoMap = new HashMap<String, AsyncTaskInfo>();

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		for (Method method : MethodUtils.getMethodsWithAnnotation(bean.getClass(), AsyncTask.class)) {
			handleMethod(bean, method);
		}
		for (Method method : MethodUtils.getMethodsWithAnnotation(bean.getClass().getSuperclass(),
				AsyncTask.class)) {
			handleMethod(bean, method);
		}
		if (bean instanceof AsyncTaskExecutor) {
			AsyncTaskExecutor new_name = (AsyncTaskExecutor) bean;
			new_name.setTaskInfoMap(taskInfoMap);
		}
		return bean;
	}

	void handleMethod(Object bean, Method method) {
		String key = JSON.toJSONString(method.getParameterTypes());
		if (!key.startsWith("[" + JSON.toJSONString(AsyncTaskContext.class))) {
			throw new RuntimeException("第一个参数必须为" + AsyncTaskContext.class + "," + method);
		}
		if (method.getParameters().length <= 1) {
			throw new RuntimeException("至少两个参数," + method);
		}
		log.info("AsyncTask added,{}", method);
		taskInfoMap.put(key, new AsyncTaskInfo(bean,method));
	}

}
