package com.xlmkit.springboot.support.imagecode;

import lombok.Data;

import java.io.Serializable;

@Data
	public  class ImageCode implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String id;
		private String name;
		private String code;
		private long createTime;
		private long validateCount;
		private boolean validate = false;
	}