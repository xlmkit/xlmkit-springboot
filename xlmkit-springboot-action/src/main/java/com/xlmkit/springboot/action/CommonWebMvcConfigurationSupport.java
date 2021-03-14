package com.xlmkit.springboot.action;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;


public class CommonWebMvcConfigurationSupport extends WebMvcConfigurationSupport {

	public @Autowired ApplicationContext context;

	@Override
	protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		super.configureMessageConverters(converters);
		converters.addAll(WebMvcConfigurationUtils.configureMessageConverters());

	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		super.addArgumentResolvers(argumentResolvers);
		argumentResolvers.addAll(WebMvcConfigurationUtils.argumentResolvers(context));
	}

	@Bean
	public ActionArgumentResolver JSONBodyHandlerMethodArgumentResolver() {
		return new ActionArgumentResolver();
	}
}
