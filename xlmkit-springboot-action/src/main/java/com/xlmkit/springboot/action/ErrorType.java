package com.xlmkit.springboot.action;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorType {
	private String err_code;
	private String err_des;
}