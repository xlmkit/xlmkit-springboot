package com.xlmkit.springboot.jpa.matcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JavacodeMatch {
	private static Pattern PATTERN = Pattern.compile("^--[\\s]+javacode ([\\s\\S]*)$");
	private boolean find;
	private String content;

	public static JavacodeMatch match(String content) {
		Matcher matcher = PATTERN.matcher(content);
		if (matcher.find()) {
			return new JavacodeMatch(true, matcher.group(1));
		}
		return new JavacodeMatch(false, null);
	}
}
