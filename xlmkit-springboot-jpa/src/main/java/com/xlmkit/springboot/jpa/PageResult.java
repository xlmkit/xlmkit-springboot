package com.xlmkit.springboot.jpa;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.alibaba.fastjson.JSONObject;

/**
 * @author 小龙码
 * @param <T>
 * 分页查询结果实现类
 */
public class PageResult<T> extends PageImpl<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JSONObject ext;

	public PageResult(List<T> content, Pageable pageable, long total, JSONObject ext) {
		super(content, pageable, total);
		this.ext = ext;
	}

	public JSONObject getExt() {
		return ext;
	}
}
