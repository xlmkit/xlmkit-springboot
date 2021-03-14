package com.xlmkit.springboot.jpa.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.util.StringUtils;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;

public class Joiner {
	public static String SEPARATOR = ",";

	public static <T> String join(Collection<T> list, String s, JoinValue<T> joinValue) {
		if (list == null || list.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (T t : list) {
			sb.append(s);
			sb.append(joinValue.getValue(t));
		}
		return sb.substring(s.length());
	}

	public static String join(Collection<?> list, String s) {
		return join(list, s, t -> {
			return t.toString();
		});
	}

	public static String join(Collection<?> list) {
		return join(list, SEPARATOR, t -> {
			return t.toString();
		});
	}

	public static <T> String join(Collection<T> list, JoinValue<T> joinValue) {
		return join(list, SEPARATOR, joinValue);
	}

	public static List<Long> parseToLong(String str, String s) {
		return parse(str, Long.class, s);
	}

	public static List<Long> longParse(String str) {
		return parse(str, Long.class, SEPARATOR);
	}

	public static List<String> stringParse(String str) {
		return parse(str, String.class, SEPARATOR);
	}

	public static List<String> stringParse(String str, String separator) {
		return parse(str, String.class, separator);
	}

	public static <T> List<T> parse(String str, Class<T> type, String s) {
		List<T> list = new ArrayList<T>();
		if (StringUtils.isEmpty(str)) {
			return list;
		}
		for (String item : str.split(s)) {
			if (!StringUtils.isEmpty(item)) {
				list.add(TypeUtils.cast(item, type, ParserConfig.global));
			}
		}
		return list;
	}

	public static interface JoinValue<T> {
		Object getValue(T t);
	}

	public static String clearup(String string, String s) {
		return join(parse(string, String.class, s));
	}
}
