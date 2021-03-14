package com.xlmkit.springboot.action.sdk;

import org.apache.commons.lang3.reflect.FieldUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultHelper<T> {

	public boolean isSuccess() {
		String return_code = null;
		String result_code = null;
		try {
			return_code = readField(this, "return_code") + "";
		} catch (Exception e) {
		}
		try {
			result_code = readField(this, "result_code") + "";
		} catch (Exception e) {
			return false;
		}
		if (return_code == null) {
			return "SUCCESS".equals(result_code);
		}
		return "SUCCESS".equals(return_code) && "SUCCESS".equals(result_code);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <K> K normalError(Class<?> clazz,String message) {

		ResultHelper t;
		try {
			t = (ResultHelper) clazz.newInstance();
			writeField(t, "result_code", "FAIL");
			writeField(t, "err_code", "NORMAL_ERROR");
			writeField(t, "err_des", message);
			return  (K) t;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <K> K normalError(Class<?> clazz,String err_code,String message) {

		ResultHelper t;
		try {
			t = (ResultHelper) clazz.newInstance();
			writeField(t, "result_code","FAIL");
			writeField(t, "err_code", err_code);
			writeField(t, "err_des", message);
			return  (K) t;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

	@SuppressWarnings("unchecked")
	public T success() {
		writeField(this, "result_code", "SUCCESS");
		return (T) this;
	}

	public static void writeField(Object object, String name, Object value) {
		try {
			FieldUtils.writeField(object, name, value, true);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static Object readField(Object object, String name) {
		try {
			return FieldUtils.readField(object, name, true);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}