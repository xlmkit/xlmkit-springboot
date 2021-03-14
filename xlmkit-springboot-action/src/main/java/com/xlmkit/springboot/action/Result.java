package com.xlmkit.springboot.action;

import java.util.Date;
import java.util.HashMap;

import org.slf4j.helpers.MessageFormatter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;

public class Result extends HashMap<String, Object> {

	public static ErrorType PARAMETER_ERROR = new ErrorType("PARAMETER_ERROR", "参数错误");
	public static ErrorType SYSTEM_ERROR = new ErrorType("SYSTEM_ERROR", "系统异常");
	public static ErrorType ACCESS_ERROR = new ErrorType("ACCESS_ERROR", "访问异常");
	public static ErrorType NORMAL_ERROR = new ErrorType("NORMAL_ERROR", "通常异常");

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Result() {
		put("return_code", "SUCCESS");
		put("result_time", new Date());
	}

	public static Result success() {
		return new Result().put("result_code", "SUCCESS").put("message", "请求成功");
	}

	public static Result success(String name, Object value) {
		return success().put(name, value);
	}

	public Result put(Object object) {
		super.putAll((JSONObject) JSON.toJSON(object));
		return this;
	}

	public static Result page(Object object) {
		return success("page", object);
	}

	public static Result list(Object object) {
		return success("list", object);
	}
	public static Result data(Object object) {
		return success("data", object);
	}
	@Override
	public Result put(String name, Object value) {
		super.put(name, value);
		return this;
	}

	public void throwException() {
		throw new ResultException(this);
	}


	public static Result fail(ErrorType type) {
		Result self = new Result();
		self.put("result_code", "FAIL");
		self.put("err_code", type.getErr_code());
		self.put("err_des", type.getErr_des());
		return self;
	}

	public static Result fail(ErrorType type, String... dess) {
		Result self = new Result();
		self.put("result_code", "FAIL");
		self.put("err_code", type.getErr_code());
		self.put("err_des", type.getErr_des() + "@" + Joiner.on("@").skipNulls().join(dess));
		return self;
	}

	public static Result KNOWN_ERROR(ErrorType type, String des) {
		Result self = new Result();
		self.put("result_code", "FAIL");
		self.put("err_code", type.getErr_code());
		self.put("err_des", type.getErr_des() + "@" + des);
		return self;
	}

	public static Result normalError(String message) {
		Result self = new Result();
		self.put("result_code", "FAIL");
		self.put("err_code", NORMAL_ERROR.getErr_code());
		self.put("err_des", message);
		return self;
	}
	public static Result normalError(String message,Object ...args) {
		Result self = new Result();
		self.put("result_code", "FAIL");
		self.put("err_code", NORMAL_ERROR.getErr_code());
		self.put("err_des", MessageFormatter.arrayFormat(message, args).getMessage());
		return self;
	}
	public static Result fail(ErrorType type, String des) {
		Result self = new Result();
		self.put("result_code", "FAIL");
		self.put("err_code", type.getErr_code());
		self.put("err_des", type.getErr_des() + "@" + des);
		return self;
	}

}
