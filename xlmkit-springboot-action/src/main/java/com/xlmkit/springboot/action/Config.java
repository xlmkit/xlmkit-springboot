package com.xlmkit.springboot.action;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class Config {
	private List<Class<?>> basePackageClasses;
	private String separator;
	private String prefix;
	public Config(Class<?>[] basePackageClasses, String separator, String prefix) {
		super();
		this.basePackageClasses = Arrays.asList(basePackageClasses);
		this.separator = separator;
		this.prefix = prefix;
	}
	
	
}
