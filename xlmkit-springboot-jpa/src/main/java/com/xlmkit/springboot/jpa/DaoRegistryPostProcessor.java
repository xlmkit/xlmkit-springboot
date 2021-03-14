package com.xlmkit.springboot.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.xlmkit.springboot.jpa.annotation.Model;
import com.xlmkit.springboot.jpa.util.Joiner;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.Assert;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DaoRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, InvocationHandler {
	public Class<?>[] packageClasses;
	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js");
	private ConfigurableListableBeanFactory beanFactory;
	private XSQLHandler sqlHandler;
	private XHQLHandler hqlHandler;
	private Map<String, MethodContext> methodInvokerMap = new HashMap<String, MethodContext>();
	private boolean inDevelopment = true;
	private String tablePrefix;

	public DaoRegistryPostProcessor(Class<?>[] packageClasses, String tablePrefix) {
		super();
		this.packageClasses = packageClasses;
		this.tablePrefix = tablePrefix;
		String cmd = System.getProperty("sun.java.command");
		inDevelopment = cmd != null && (cmd.endsWith("Application") || cmd.endsWith("Starter"));
		log.info("inDevelopment={}", inDevelopment);
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		for (Class<?> clazz : packageClasses) {
			log.info("******** package={}", clazz.getPackage());
			doPackage(clazz, beanFactory);
			if (!tablePrefix.equals("")) {
				EntityModifier.update(tablePrefix, clazz.getAnnotation(Model.class).value());
			}
			log.info("********");
		}
	}

	private void doPackage(Class<?> packageClass, ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		try {
			String packageName = packageClass.getPackage().getName().replaceAll("\\.", "/");
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resolver);
			Resource[] resources = resolver.getResources("classpath*:" + packageName + "/*.class");
			for (Resource r : resources) {
				MetadataReader reader = metaReader.getMetadataReader(r);
				doClass(reader, beanFactory);
			}
		} catch (Exception e) {
			throw new RuntimeException(packageClass + "", e);
		}

	}

	private void doClass(MetadataReader reader, ConfigurableListableBeanFactory beanFactory)
			throws ClassNotFoundException, IOException, ScriptException {

		String className = reader.getClassMetadata().getClassName();
		Class<?> daoClass = Class.forName(className);
		if (!daoClass.isInterface()) {
			log.debug("ignore={}", daoClass);
			return;
		}
		String name = daoClass.getName();
		Class<?>[] classzz = new Class[] { daoClass };

		Properties properties = readProps(daoClass);
		for (java.lang.reflect.Method method : daoClass.getMethods()) {
			methodInvokerMap.put(method.toString(), initMethodContext(method, properties));
		}

		Object bean = Proxy.newProxyInstance(getClass().getClassLoader(), classzz, this);
		beanFactory.registerSingleton(name, bean);
		log.info(">> register={}", name);

	}

	private MethodContext initMethodContext(Method method, Properties properties) {
		String propKey = method.getName() + "#";
		for (Parameter parameter : method.getParameters()) {
			propKey += "#" + parameter.getParameterizedType().getTypeName();
		}
		String comment = properties.getProperty(propKey);
		comment = new String(Base64.getDecoder().decode(comment), StandardCharsets.UTF_8);
		String names = properties.getProperty("parameter.names#" + propKey, "");

		List<String> paramNames = Joiner.parse(names, String.class, ",");
		int a = comment.indexOf("<pre>");
		int b = comment.indexOf("</pre>");

		// System.out.println(propKey);
		// System.out.println("xxxx:\n"+comment);
		String preComment = null;
		String script = null;
		try {
			preComment = comment.substring(a + 5, b);
			script = new SQLBuilder().build(preComment, method, paramNames);
			script = script.replaceFirst("------------------------------------------------------------------------",
					"");
		} catch (Exception e) {
			log.info("method={}", method);
			log.info("preComment=\n{}", comment);
			log.info("script=\n{}", script);
			throw e;
		}

		try {
			engine.eval(script);
		} catch (Exception e) {
			int i = 0;
			log.error("脚本错误", method);
			for (String item : script.split("\n")) {
				log.error(i + " " + item);
				i++;
			}
			throw new RuntimeException(e);
		}
		return new MethodContext(engine, method, script);
	}

	public static Properties readProps(Class<?> daoClass) throws IOException {
		Properties properties = new Properties();
		String file = daoClass.getSimpleName() + ".comment.properties";
		InputStream is = daoClass.getResourceAsStream(file);
		Assert.notNull(is, file + " not exist! please check [comment-helper]");
		properties.load(is);
		return properties;
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

	}



	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (sqlHandler == null) {
			sqlHandler = beanFactory.getBean(XSQLHandler.class);
			hqlHandler = beanFactory.getBean(XHQLHandler.class);
		}
		if (method.getName().equals("toString")) {
			return proxy.getClass().toString();
		}
		args = args == null ? new Object[] {} : args;

		MethodContext invoker = methodInvokerMap.get(method.toString());
		if (inDevelopment) {
			Properties properties = readProps(method.getDeclaringClass());
			invoker = initMethodContext(method, properties);
		}
		if (!invoker.isHQL()) {
			return sqlHandler.execute(invoker, args);
		} else {
			return hqlHandler.execute(invoker, args);
		}

	}

}
