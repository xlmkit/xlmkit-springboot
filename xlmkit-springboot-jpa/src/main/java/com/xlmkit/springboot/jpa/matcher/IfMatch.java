package com.xlmkit.springboot.jpa.matcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IfMatch {
	private static Pattern PATTERN = Pattern.compile("^--[\\s]+if([\\s\\S]*)$");
	private boolean find;
	private String content;

	public static IfMatch match(String content) {
		Matcher matcher = PATTERN.matcher(content);
		if (matcher.find()) {
			return new IfMatch(true, matcher.group(1));
		}
		return new IfMatch(false, null);
	}

}
