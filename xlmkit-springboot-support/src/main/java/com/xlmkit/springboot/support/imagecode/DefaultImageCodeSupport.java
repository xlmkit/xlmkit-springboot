package com.xlmkit.springboot.support.imagecode;

import com.xlmkit.springboot.action.Result;
import com.xlmkit.springboot.support.thirdparty.expiringmap.ExpirationPolicy;
import com.xlmkit.springboot.support.thirdparty.expiringmap.ExpiringMap;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DefaultImageCodeSupport implements ImageCodeSupport{
	@Getter
	@Setter
	private ExpiringMap<String,ImageCode> expiringMap;
	public DefaultImageCodeSupport(){

		expiringMap = ExpiringMap.builder()
				.maxSize(1000)
				.expiration(5, TimeUnit.MINUTES)
				.expirationPolicy(ExpirationPolicy.ACCESSED)
				.variableExpiration()
				.build();

	}
	/**
	 * 是否是调试
	 */
	@Getter
	@Setter
	private boolean debug = false;

	private void save(ImageCode imageCodeCache) {
		expiringMap.put(imageCodeCache.getId(),imageCodeCache);
	}
	private ImageCode get(String s) {
		return expiringMap.get(s);
	}

	@Override
	public Result create(String name) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		String code = RandomStringUtils.random(4, VerifyCodeUtils.VERIFY_CODES);
		try {
			VerifyCodeUtils.outputImage(220, 80, os, code);
		} catch (IOException e) {
			return Result.normalError("验证码生成失败");
		}
		String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(os.toByteArray());
		ImageCode imageCodeCache = new ImageCode();
		imageCodeCache.setId(UUID.randomUUID().toString());
		imageCodeCache.setCreateTime(System.currentTimeMillis());
		imageCodeCache.setName(name);
		imageCodeCache.setCode(code);
		Result res = Result.success();
		res.put("token", imageCodeCache.getId());
		res.put("base64", base64);
		if (debug) {
			res.put("debugCode", code);
		}
		save(imageCodeCache);
		return res;
	}


	@Override

	public boolean validate(String name, String imageCodeData) {
		String[] array = imageCodeData.split("\\@");
		if (array.length != 2) {
			return false;
		}
		ImageCode cache = get(array[0]);
		if (cache == null) {
			return false;
		}
		if (!cache.isValidate()) {
			readyValidateByData(imageCodeData);
			cache = get(array[0]);
		}
		return cache.isValidate() && name.equals(cache.getName()) && cache.getCode().equalsIgnoreCase(array[1]);
	}



	@Override
	public Result readyValidate(String token, String code) {
		ImageCode cache = get(token);
		if (cache == null) {
			return Result.normalError("验证码过期0，请更换！");
		}
		if (cache.getValidateCount() > 3) {
			return Result.normalError("多次验证错误，请更换！");
		}
		if (System.currentTimeMillis() - cache.getCreateTime() > 1000 * 60 * 5) {
			return Result.normalError("验证码过期1，请更换！");
		}
		if (cache.isValidate() && cache.getCode().equalsIgnoreCase(code)) {
			return Result.success();
		}
		if (cache.getCode().equalsIgnoreCase(code)) {
			cache.setValidate(true);
			save(cache);
			return Result.success();
		} else {
			cache.setValidateCount(cache.getValidateCount() + 1);
			save(cache);
			return Result.normalError("验证码错误！");
		}
	}
	@Override
	public Result readyValidateByData(String imageCodeData) {
		String[] array = imageCodeData.split("\\@");
		if (array.length != 2) {
			return Result.normalError("数据格式错误");
		}
		return readyValidate(array[0], array[1]);
	}
}
