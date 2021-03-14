package com.xlmkit.springboot.action;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActionService {

	@AllArgsConstructor
	private class ClassItem {
		Config config;
		Class<?> actionClass;
	}

	private @Autowired RequestMappingHandlerMapping mapping;
	private @Autowired ApplicationContext context;
	
	public List<ClassItem> actionClasses = new ArrayList<ClassItem>();
	public Map<Object, Object> proxyMap = new HashMap<Object, Object>();
	public List<Method> methods = new ArrayList<Method>();

	public ActionService(List<Config> configs) {
		for (Config config : configs) {
			for (Class<?> clazz : config.getBasePackageClasses()) {
				doPackage(config, clazz);
			}
		}
	}
	private void doPackage(Config config, Class<?> packageClass) {
		try {
			String packageName = packageClass.getPackage().getName().replaceAll("\\.", "/");
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resolver);
			Resource[] resources = resolver.getResources("classpath*:" + packageName + "/*.class");
			for (Resource r : resources) {
				MetadataReader reader = metaReader.getMetadataReader(r);
				doClass(config, reader);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void doClass(Config config, MetadataReader reader)
			throws ClassNotFoundException, IOException, ScriptException {

		String className = reader.getClassMetadata().getClassName();
		Class<?> actionClass = Class.forName(className);
		if (!actionClass.isInterface() || !actionClass.getName().endsWith("Action")) {
			log.debug("ignore={}", actionClass);
			return;
		}
		actionClasses.add(new ClassItem(config, actionClass));

	}

	@PostConstruct
	public void addMapping() throws NoSuchMethodException {

		for (ClassItem item : actionClasses) {
			String actionName = item.actionClass.getSimpleName().replaceAll("Action", "");
			Object action = null;
			try {
				action = context.getBean(item.actionClass);
			} catch (Exception e) {
				log.warn("Action Bean No Found!!!{}", item.actionClass);
				continue;
			}
			for (Method method : item.actionClass.getDeclaredMethods()) {
				String path = item.config.getPrefix() + "/" + actionName + item.config.getSeparator()
						+ method.getName();
				RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(path)//
						.methods(RequestMethod.POST)//
						.produces(MediaType.APPLICATION_JSON_VALUE,"application/x-json")//
						.build();
				methods.add(method);
				mapping.registerMapping(requestMappingInfo, action, method);
			}
		}
	}
	public List<Method> getMethods() {
		return methods;
	}


}