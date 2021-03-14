package com.xlmkit.springboot.support.asynctask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.alibaba.fastjson.JSONObject;

import lombok.Setter;

/**
 * 任务执行器
 */
public class AsyncTaskExecutor {
	private Map<String, AsyncTaskContext> taskMap = new HashMap<String, AsyncTaskContext>();
	private @Setter Map<String, AsyncTaskInfo> taskInfoMap = new HashMap<String, AsyncTaskInfo>();
	private @Autowired ApplicationContext context;
	private  ExecutorService executorService = Executors.newCachedThreadPool();;
	@Setter
	@Getter
	private long timeout = 10000;


	public AsyncTaskContext getTask(String id) {
		return taskMap.get(id);
	}


	public void ab(int a,int b){

	}
	public AsyncTaskContext submit(Object... args) throws NoSuchMethodException {
		List<Object> newArgs = new ArrayList<>();
		List<Class<?>> classes = new ArrayList<>();
		classes.add(AsyncTaskContext.class);
		for(int i = 0;i<args.length;i+=2) {
			classes.add((Class<?>)args[i]);
			newArgs.add(args[i+1]);
		}
		String actionKey = JSONObject.toJSONString(classes);
		AsyncTaskInfo taskInfo = taskInfoMap.get(actionKey);
		if (taskInfo == null) {
			throw new NoSuchMethodException();
		}
		AsyncTaskContext context = new AsyncTaskContext(taskInfo.getMethod());
		newArgs.add(0, context);
		taskMap.put(context.getId(), context);
		executorService.submit(() -> {
			this.invoke(context, taskInfo, newArgs);
		});
		return context;

	}
	private void invoke(AsyncTaskContext context, AsyncTaskInfo taskInfo, List<Object> parameters) {
		try {
			Object[] args = new Object[parameters.size()];
			parameters.toArray(args);
			taskInfo.getMethod().invoke(taskInfo.getBean(),args);
		} catch (Exception e) {
			context.stop("MethodInvoker异常", e);
		} finally {
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					taskMap.remove(context.getId());

				}
			}, timeout);
		}
	}

}
