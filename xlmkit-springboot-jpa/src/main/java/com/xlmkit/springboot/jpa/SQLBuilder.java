package com.xlmkit.springboot.jpa;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xlmkit.springboot.jpa.matcher.ForMatcher;
import com.xlmkit.springboot.jpa.matcher.IfMatch;
import com.xlmkit.springboot.jpa.matcher.JavacodeMatch;
import com.xlmkit.springboot.jpa.util.MD5;
import org.slf4j.helpers.MessageFormatter;

public class SQLBuilder {
	private StringBuilder code = new StringBuilder();
	private final String LIST_NAME = QueryType.class.getCanonicalName()+"."+QueryType.LIST.name();
	private final String COUNT_NAME = QueryType.class.getCanonicalName()+"."+QueryType.COUNT.name();
	public String build(String sql, Method method, List<String> names) {
		// String sql = config.getSql();

		pushCode("function $empty( obj){");
		pushCode("	if(obj==null)");
		pushCode("		return true;");
		pushCode("	return \"\".equals(obj);");
		pushCode("}");
		pushCode("function check1( obj){");
		pushCode("	if(obj==null)");
		pushCode("		return false;");
		pushCode("	return !\"\".equals(obj);");
		pushCode("}");
		pushCode("function check3( keyword, keyword_type, expected){");
		pushCode("	if($empty(keyword)){");
		pushCode("		return false;");
		pushCode("	}");
		pushCode("	if(expected==null&&$empty(keyword_type)){");
		pushCode("		return true;");
		pushCode("	}");
		pushCode("	if(expected==null){");
		pushCode("		return false;");
		pushCode("	}");
		pushCode("	return expected.equals(keyword_type);");
		pushCode("}");
		pushCode("function check2( keyword,keyword_type){");
		pushCode("	return check3(keyword,keyword_type,null);");
		pushCode("}");
		pushCode("function check( keyword, keyword_type, expected){");
		pushCode("	if(arguments.length===1){");
		pushCode("		return check1(keyword);");
		pushCode("	}");
		pushCode("	if(arguments.length===2){");
		pushCode("		return check2(keyword,keyword_type);");
		pushCode("	}");
		pushCode("	if(arguments.length===3){");
		pushCode("		return check3(keyword,keyword_type,expected);");
		pushCode("	}");
		pushCode("	return false;");
		pushCode("}");
		pushCode("function _" + MD5.MD5_32bit(method.toString()) + "($");
		for (String name : names) {
			pushCode(",{}", name);
		}
		pushCode("){");

		while (true) {
			Matcher matcher = Pattern.compile("(-- [^\n]*)").matcher(sql);
			if (matcher.find()) {
				String _content = sql.substring(matcher.start(), matcher.end()).trim();
				if (_content.equals("-- COUNT SQL")) {
					String preString = sql.substring(0, matcher.start());
					int index = findIndex("SELECT", preString);

					String a = sql.substring(0, index);
					String b = sql.substring(index, matcher.start());
					String c = sql.substring(matcher.end());
					StringBuilder sb = new StringBuilder();
					sb.append(a);
					sb.append("\n");
					sb.append("##JAVACODE>> if($.queryType()=="+COUNT_NAME+"){");
					sb.append("\n");
					sb.append(b);
					sb.append("\n");
					sb.append("##JAVACODE>> }");
					sb.append("\n");
					sb.append(c);
					sql = sb.toString();
					continue;
				}
				if (_content.equals("-- LIST SQL")) {
					String preString = sql.substring(0, matcher.start());
					int index = findIndex("SELECT", preString);

					String a = sql.substring(0, index);
					String b = sql.substring(index, matcher.start());
					String c = sql.substring(matcher.end());
					StringBuilder sb = new StringBuilder();
					sb.append(a);
					sb.append("\n");
					sb.append("##JAVACODE>> if($.queryType()=="+LIST_NAME+"){");
					sb.append("\n");
					sb.append(b);
					sb.append("\n");
					sb.append("##JAVACODE>> }");
					sb.append("\n");
					sb.append(c);
					sql = sb.toString();
					continue;
				}
				JavacodeMatch javacodeMatch = JavacodeMatch.match(_content);
				if (javacodeMatch.isFind()) {
					sql = sql.substring(0, matcher.start()) + "\n##JAVACODE>>" + javacodeMatch.getContent()
							+ sql.substring(matcher.end());
					continue;
				}

				IfMatch ifMatch = IfMatch.match(_content);
				if (ifMatch.isFind()) {

					String preString = sql.substring(0, matcher.start());
					int startIndex;
					if (preString.trim().endsWith(")")) {
						int index = findIndex("(", preString);

						startIndex = preString.lastIndexOf('\n', index);
						sql = sql.substring(0, matcher.start()) + "\n##JAVACODE>>}\n" + sql.substring(matcher.end());

					} else {
						startIndex = preString.lastIndexOf('\n');
						sql = sql.substring(0, matcher.start()) + "\n##JAVACODE>>}\n" + sql.substring(matcher.end());
					}
					sql = new StringBuilder(sql).insert(startIndex, "##JAVACODE>>if(" + ifMatch.getContent() + "){")
							.toString();
					continue;
				}
				// 默认
				sql = sql.substring(0, matcher.start()) + sql.substring(matcher.end());
			} else {
				break;
			}
		}

		{
			Pattern pattern = Pattern.compile("(\\{[^}^\n]*\\})");
			while (true) {
				Matcher matcher = pattern.matcher(sql);
				if (matcher.find()) {
					String start = sql.substring(0, matcher.start());
					String content = sql.substring(matcher.start() + 1, matcher.end() - 1).trim();
					String end = sql.substring(matcher.end());
					StringBuilder sb = new StringBuilder();
					doVar(start, sb, content, end);
					sql = sb.toString();
				} else {
					break;
				}
			}
		}

		sql = sql.replaceAll("##JAVACODE", "\n##JAVACODE");
		for (String item : sql.split("\n")) {
			item = item.trim();
			if (item.equals("")) {
				continue;
			}
			if (item.startsWith("##JAVACODE>>")) {
				String str = item.substring(12);
				if (!item.equals("")) {
					pushCode("{}", str);
				}

			} else {
				pushCode("$.query().append(\"{} \");", item);
			}
			// System.out.println(item);
		}
		pushCode("}");
		return code.toString();
	}

	private void doVar(String start, StringBuilder sb, String b, String end) {

		ForMatcher m = ForMatcher.match(b);
		if (m.isFind()) {

			start = start.trim();
			StringBuilder st = new StringBuilder();

			st.append("##JAVACODE>>if (" + m.getListName() + " != null && " + m.getListName() + ".size() > 0) {\n");
			if (start.matches("^[\\s\\S]*[Aa]{1}[Nn]{1}[Dd]{1}$")) {
				start = start.substring(0, start.length() - 3);
				st.append(" AND \n");
			} else if (start.matches("^[\\s\\S]*[Oo]{1}[Rr]{1}$")) {
				start = start.substring(0, start.length() - 2);
				st.append(" OR \n");
			}
			appendJcode(st, "$.query().append(\"(\");");
			appendJcode(st, "var i = 0;");
			appendJcode(st, "for (var item in " + m.getListName() + ") {");
			appendJcode(st, "if(i==0) {");

			if (m.getOperator().equals("FIND_IN_SET")) {
				appendJcode(st, "$.query().append(\" FIND_IN_SET( ?, " + m.getParamName() + " ) \");");
			} else {
				appendJcode(st, "$.query().append(\" " + m.getParamName() + " " + m.getOperator() + " ? \");");
			}

			appendJcode(st, "$.placeholders().add(" + m.getListName() + ".get(item));");

			appendJcode(st, "}else {");

			if (m.getOperator().equals("FIND_IN_SET")) {
				appendJcode(st, "$.query().append(\" " + m.getConjunctionName() + " FIND_IN_SET( ?, " + m.getParamName()
						+ " ) \");");
			} else {
				appendJcode(st, "$.query().append(\"" + m.getConjunctionName() + " " + m.getParamName() + " "
						+ m.getOperator() + " ? \");");
			}

			appendJcode(st, "$.placeholders().add(" + m.getListName() + ".get(item));");
			appendJcode(st, "}");
			appendJcode(st, "i++;");
			appendJcode(st, "}");
			appendJcode(st, "$.query().append(\")\");");
			appendJcode(st, "}");

			sb.append(start);
			sb.append("\n");
			sb.append(st.toString());
			sb.append(end);
			sb.append("\n");
			return;
		}
		if (b.startsWith("@insert"))

		{
			sb.append(start);
			sb.append("\n");
			sb.append("\n");
			sb.append("##JAVACODE>>$.query().append(');");
			sb.append("##JAVACODE>>$.query().append(" + b.substring(6) + "\" \");");
			sb.append("\n");
			sb.append(end);
			sb.append("\n");
			return;
		}
		if (b.startsWith("@text")) {
			sb.append(start);
			sb.append("\n");
			sb.append("\n");
			sb.append("##JAVACODE>>$.query().append(\"\'\");");
			sb.append("##JAVACODE>>$.query().append(" + b.substring(6) + "+\"\");");
			sb.append("##JAVACODE>>$.query().append(\"'\" );");
			sb.append("\n");
			sb.append(end);
			sb.append("\n");
			return;
		}
		sb.append(start);
		sb.append("\n");
		sb.append("\n");
		sb.append("?");
		sb.append("\n");
		sb.append("##JAVACODE>>$.placeholders().add(" + b + ");");
		sb.append("\n");
		sb.append(end);
		sb.append("\n");
	}

	private void appendJcode(StringBuilder st, String string) {
		st.append("##JAVACODE>>");
		st.append(string);
		st.append("\n");
	}

	public void pushCode(String str, Object... args) {
		code.append(MessageFormatter.arrayFormat(str, args).getMessage());
		code.append("\n");
	}

	public static int findIndex(String p, String str) {
		int end = str.length();
		while (true) {
			int a = str.lastIndexOf(p, end);
			String sub = str.substring(a);
			Map<Character, Integer> charCount = charCount(sub);
			int b = charCount.getOrDefault(Character.valueOf('('), 0);
			int c = charCount.getOrDefault(Character.valueOf(')'), 0);
			if (b == c) {
				return a;
			}
			end = a - 1;
		}
	}

	public static Map<Character, Integer> charCount(String str) {
		HashMap<Character, Integer> map = new HashMap<>();
		for (int i = 0; i < str.length(); i++) {
			if (map.containsKey(str.charAt(i))) {
				map.put(str.charAt(i), map.get(str.charAt(i)) + 1);
			} else {
				map.put(str.charAt(i), 1);
			}
		}
		return map;
	}

	public static void main(String[] args) {
		System.out.println("  AnD".matches("^[\\s\\S]*[Aa]{1}[Nn]{1}[Dd]{1}$"));
	}
}
