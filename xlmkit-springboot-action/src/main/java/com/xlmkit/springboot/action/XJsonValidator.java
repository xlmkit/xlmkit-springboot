package com.xlmkit.springboot.action;

import com.xlmkit.springboot.action.sdk.XJson;
import org.springframework.core.MethodParameter;


public interface XJsonValidator {
	void validate(XJson json, MethodParameter parameter);
}
