package com.xlmkit.springboot.support.asynctask;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import com.google.common.base.Throwables;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncTaskContext {

	private String id = UUID.randomUUID().toString();
	private List<String> messages = new ArrayList<String>();
	private Method method;
	private boolean stoped = false;

	public AsyncTaskContext(Method method) {
		super();
		this.method = method;
	}


	public String getId() {
		return id;
	}

	public synchronized List<String> clearMessages() {
		List<String> ms = new ArrayList<>(messages);
		messages.clear();
		return ms;
	}

	public void info(String format, Object... args) {
		if (stoped) {
			log.error("{}>stoped",method);
		}
		log.info(method + ">" + format, args);
		FormattingTuple tuple = MessageFormatter.arrayFormat(format, args);
		messages.add(tuple.getMessage());
		if (tuple.getThrowable() != null) {
			messages.add(Throwables.getStackTraceAsString(tuple.getThrowable()));
		}

	}

	public boolean isStoped() {
		return stoped;
	}

	public void stop(String format, Object... args) {
		if (stoped) {
			log.error(method + ">stoped");
		}
		stoped = true;
		log.info(method + ">" + format, args);
		FormattingTuple tuple = MessageFormatter.arrayFormat(format, args);
		messages.add(tuple.getMessage());
		if (tuple.getThrowable() != null) {
			messages.add(Throwables.getStackTraceAsString(tuple.getThrowable()));
		}
	}

}
