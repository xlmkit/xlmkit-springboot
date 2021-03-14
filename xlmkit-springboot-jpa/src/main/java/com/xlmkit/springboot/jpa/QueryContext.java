package com.xlmkit.springboot.jpa;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 小龙码
 * 查询上下文
 */
public class QueryContext {

	private QueryType queryType;

	private StringWriter query = new StringWriter();

	private List<Object> placeholders = new ArrayList<Object>();

	private JSONObject attrs = new JSONObject();

	public QueryContext(QueryType queryType) {
		this.queryType = queryType;
	}



	public StringWriter attr(String key, Object value) {
		attrs.put(key, value);
		return query;
	}

	public JSONObject attrs() {
		return attrs;
	}

	public QueryType queryType() {
		return queryType;
	}

	public StringWriter query() {
		return query;
	}

	public List<Object> placeholders() {
		return placeholders;
	}
}
