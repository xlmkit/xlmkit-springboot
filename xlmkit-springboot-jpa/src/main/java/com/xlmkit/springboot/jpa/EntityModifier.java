package com.xlmkit.springboot.jpa;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Table;


import com.xlmkit.springboot.jpa.util.PkgUtil;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class EntityModifier {
	public static int updateIndex = 0;

	public static void update(String tablePrefix, Class<?> packageClass) {
		for (Class<?> item : PkgUtil.getClzFromPkg(packageClass.getPackage().getName())) {
			Entity entity = item.getAnnotation(Entity.class);
			if (entity != null) {
				doEntity(updateIndex, item, entity, item.getAnnotation(Table.class), tablePrefix);
			}

		}
		updateIndex++;
	}

	private static void doEntity(int index, Class<?> item, Entity entity, Table table, String tablePrefix) {
		if (table == null) {
			throw new RuntimeException("Entity need Annotated @Table," + item);
		}
		try {
			updateEntity(entity, item.getSimpleName() + "." + index);
			log.info("修改Entity,class={},name={}",item,item.getSimpleName() + "." + index);
		} catch (Exception e) {
			throw new RuntimeException("修改entity出错，" + item, e);
		}
		try {
			updateTable(table, tablePrefix + item.getSimpleName());
			log.info("修改Table,class={},name={}",item,tablePrefix + item.getSimpleName());
		} catch (Exception e) {
			throw new RuntimeException("修改entity出错，" + item, e);
		}

	}

	private static void updateEntity(Entity entity, String name) throws Exception {
		InvocationHandler invocationHandler = Proxy.getInvocationHandler(entity);
		Field value = invocationHandler.getClass().getDeclaredField("memberValues");
		value.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, Object> memberValues = (Map<String, Object>) value.get(invocationHandler);
		memberValues.put("name", name);

	}

	private static void updateTable(Table table, String name) throws Exception {
		InvocationHandler invocationHandler = Proxy.getInvocationHandler(table);
		Field value = invocationHandler.getClass().getDeclaredField("memberValues");
		value.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, Object> memberValues = (Map<String, Object>) value.get(invocationHandler);
		memberValues.put("name", name);

	}
}
