package com.xlmkit.springboot.jpa.matcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;

import lombok.Data;

@Data
public class ForMatcher {
	private static Pattern PATTERN = Pattern.compile("^@for[\\s]*'([^,]*)((,[^,]*)*)'[\\s]*$");
	private boolean find;
	private String listName;
	private String paramName;
	private String conjunctionName = "OR";
	private String operator = "=";

	public static ForMatcher match(String content) {
		Matcher matcher = PATTERN.matcher(content);
		ForMatcher self = new ForMatcher();
		if (matcher.find()) {
			self.find = true;
			self.listName = matcher.group(1).trim();
			String[] arr = matcher.group(2).trim().substring(1).split(",");
			if (arr.length > 0) {
				self.paramName = arr[0].trim();
			}
			if (arr.length > 1) {
				self.conjunctionName = arr[1].trim();
			}
			if (arr.length > 2) {
				self.operator = arr[2].trim();
			}
			return self;
		}
		return self;
	}

	public static void main(String[] args) {
		System.out.println(JSON.toJSONString(match("@for 'parkIds,root.id,OR' "), true));
	}

}
