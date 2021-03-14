package com.xlmkit.springboot.action;

import lombok.Getter;

@Getter
public class SDKException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String err_code;
	private String err_des;

	public SDKException(String err_code, String err_des) {
		super(err_code + "@" + err_des);
		this.err_code = err_code;
		this.err_des = err_des;
	}

	public String getErr_code() {
		return err_code;
	}

	public String getErr_des() {
		return err_des;
	}
}
