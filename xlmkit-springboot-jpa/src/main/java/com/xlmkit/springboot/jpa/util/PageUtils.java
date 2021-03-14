package com.xlmkit.springboot.jpa.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.alibaba.fastjson.JSONObject;

public class PageUtils {
	public static PageRequest of(Object body) {
		JSONObject object = (JSONObject) JSONObject.toJSON(body);
		int pageNumber = object.getInteger("pageNumber");
		int pageSize = object.getInteger("pageSize");
		pageSize = pageSize <= 0 ? 10 : pageSize;
		return PageRequest.of(pageNumber, pageSize);
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> propList(Page<JSONObject> page, Class<?> type, String key) {
		List<T> list = new ArrayList<T>();
		for (JSONObject object : page) {
			list.add((T) object.getObject(key, type));
		}
		return list;
	}

	public static void render(Page<JSONObject> page, String dataKey, String pageKey, List<JSONObject> items,
			String itemKey) {
		Map<Object, List<JSONObject>> dataMap = new HashMap<Object, List<JSONObject>>();
		for (JSONObject item : page) {
			List<JSONObject> cs = new ArrayList<>();
			item.put(dataKey, cs);
			dataMap.put(item.get(pageKey), cs);
		}
		for (JSONObject item : items) {
			dataMap.get(item.get(itemKey)).add(item);
		}
	}
}
