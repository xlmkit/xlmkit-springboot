package com.xlmkit.springboot.support.imagecode;

import com.xlmkit.springboot.action.Result;

public interface ImageCodeSupport {


	 Result create(String name);

	 void setDebug(boolean debug);

	 boolean validate(String name, String imageCodeData);

	 Result readyValidate(String token, String code);

	Result readyValidateByData(String imageCodeData);
}
