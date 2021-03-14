/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xlmkit.springboot.jpa;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

/**
 *
 *
 * @author 小龙码
 */
@Slf4j
public class EnableCommentDaoRegistrarSupport implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata am, BeanDefinitionRegistry registry) {

		Assert.notNull(am, "AnnotationMetadata must not be null!");
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

		Class<?> aClass = EnableCommentDao.class;
		if (am.getAnnotationAttributes(aClass.getName()) == null) {
			return;
		}
		log.info("registerBeanDefinitions");
		AnnotationAttributes attributes = (AnnotationAttributes) am.getAnnotationAttributes(aClass.getName());
		Class<?>[] classes = (Class<?>[]) attributes.get("value");
		String tablePrefix = attributes.getString("tablePrefix");
		registryProstProcessor(classes,tablePrefix, registry);
		registry.registerBeanDefinition(DaoService.class.getName(), new RootBeanDefinition(DaoService.class));
		registry.registerBeanDefinition(XHQLHandler.class.getName(), new RootBeanDefinition(XHQLHandler.class));
		registry.registerBeanDefinition(XSQLHandler.class.getName(), new RootBeanDefinition(XSQLHandler.class));

	}

	private void registryProstProcessor(Class<?>[] packageClasses,String tablePrefix, BeanDefinitionRegistry registry) {
		ConstructorArgumentValues cargs = new ConstructorArgumentValues();
		cargs.addGenericArgumentValue(packageClasses);
		cargs.addGenericArgumentValue(tablePrefix);
		MutablePropertyValues pvs = new MutablePropertyValues();
		RootBeanDefinition definition = new RootBeanDefinition(DaoRegistryPostProcessor.class, cargs, pvs);
		registry.registerBeanDefinition(DaoRegistryPostProcessor.class.getName(), definition);
	}

}
