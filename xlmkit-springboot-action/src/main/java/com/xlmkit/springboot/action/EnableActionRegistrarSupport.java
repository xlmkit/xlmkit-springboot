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
package com.xlmkit.springboot.action;

import java.util.Arrays;
import java.util.List;

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
 * Base class to implement {@link ImportBeanDefinitionRegistrar}s to enable
 * repository
 * 
 * @author Oliver Gierke
 */
@Slf4j
public class EnableActionRegistrarSupport implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata am, BeanDefinitionRegistry registry) {

		Assert.notNull(am, "AnnotationMetadata must not be null!");
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

		Class<?> aClass = EnableAction.class;
		if (am.getAnnotationAttributes(aClass.getName()) == null) {
			return;
		}
		log.info("registerBeanDefinitions");
		AnnotationAttributes attributes = (AnnotationAttributes) am.getAnnotationAttributes(aClass.getName());
		Class<?>[] classes = (Class<?>[]) attributes.get("value");
		String prefix = attributes.getString("prefix");
		String separator = attributes.getString("separator");
		registryProstProcessor(Arrays.asList(new Config(classes, separator, prefix)), registry);
	}

	public static void registryProstProcessor(List<Config> configs, BeanDefinitionRegistry registry) {
		ConstructorArgumentValues cargs = new ConstructorArgumentValues();
		cargs.addGenericArgumentValue(configs);
		MutablePropertyValues pvs = new MutablePropertyValues();
		RootBeanDefinition definition = new RootBeanDefinition(ActionService.class, cargs, pvs);
		registry.registerBeanDefinition(ActionService.class.getName(), definition);
		registry.registerBeanDefinition(ResultHandler.class.getName(), new RootBeanDefinition(ResultHandler.class));
	}

}
