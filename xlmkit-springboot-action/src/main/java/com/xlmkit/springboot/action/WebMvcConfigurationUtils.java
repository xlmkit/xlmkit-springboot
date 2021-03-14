package com.xlmkit.springboot.action;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

public class WebMvcConfigurationUtils {

	public static List<HttpMessageConverter<?>> configureMessageConverters() {
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
		FastJsonConfig fastJsonConfig = new FastJsonConfig();
		fastJsonConfig.setSerializerFeatures(//
				SerializerFeature.PrettyFormat, //
				SerializerFeature.WriteDateUseDateFormat, //
				SerializerFeature.SortField, //
				SerializerFeature.MapSortField, //
				SerializerFeature.DisableCircularReferenceDetect);
		List<MediaType> fastMediaTypes = new ArrayList<MediaType>();
		fastJsonConfig.setCharset(Charset.forName("UTF-8"));
		fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
		fastConverter.setSupportedMediaTypes(fastMediaTypes);
		fastConverter.setFastJsonConfig(fastJsonConfig);
		StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
		List<MediaType> mediaTypes = new ArrayList<MediaType>();
		mediaTypes.add(MediaType.TEXT_PLAIN);
		stringConverter.setSupportedMediaTypes(mediaTypes);
		converters.add(stringConverter);
		converters.add(fastConverter);
		return converters;
	}

	public static List<HandlerMethodArgumentResolver> argumentResolvers(ApplicationContext context) {
		List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
		resolvers.addAll(context.getBeansOfType(HandlerMethodArgumentResolver.class).values());
		return resolvers;
	}

}
