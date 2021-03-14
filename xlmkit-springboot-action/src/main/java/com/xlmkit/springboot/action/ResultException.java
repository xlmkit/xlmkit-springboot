package com.xlmkit.springboot.action;


import lombok.Getter;

@Getter
public class ResultException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Result result;

	public ResultException(Result result) {
		this.result = result;
	}

}
