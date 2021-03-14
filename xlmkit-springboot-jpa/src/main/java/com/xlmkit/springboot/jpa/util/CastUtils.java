package com.xlmkit.springboot.jpa.util;

import java.lang.reflect.Type;

import com.alibaba.fastjson.JSONObject;

public class CastUtils {
	public static Object cast(Object item, Type type) {
		JSONObject jsonObject = new JSONObject();
		if (item.getClass().isArray()) {
			jsonObject.put("com/xlmkit/springboot/jpa", ((Object[]) item)[0]);
		} else {
			jsonObject.put("com/xlmkit/springboot/jpa", item);
		}
		return jsonObject.getObject("com/xlmkit/springboot/jpa", type);
	}
}
