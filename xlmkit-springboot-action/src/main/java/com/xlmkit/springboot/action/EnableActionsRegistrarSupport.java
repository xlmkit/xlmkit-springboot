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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnableActionsRegistrarSupport implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

		Assert.notNull(annotationMetadata, "AnnotationMetadata must not be null!");
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

		Class<?> aClass = EnableActions.class;
		if (annotationMetadata.getAnnotationAttributes(aClass.getName()) == null) {
			return;
		}
		log.info("action:::registerBeanDefinitions");
		AnnotationAttributes x = (AnnotationAttributes) annotationMetadata.getAnnotationAttributes(aClass.getName());
		List<Config> configs = new ArrayList<>();
		Map<String, List<Class<?>>> map = new HashMap<String, List<Class<?>>>();
		for (AnnotationAttributes attributes : x.getAnnotationArray("value")) {
			Class<?>[] classes = attributes.getClassArray("value");
			String prefix = attributes.getString("prefix");
			String separator = attributes.getString("separator");
			configs.add(new Config(classes, separator, prefix));
		}
		EnableActionRegistrarSupport.registryProstProcessor(configs, registry);
	}

}
