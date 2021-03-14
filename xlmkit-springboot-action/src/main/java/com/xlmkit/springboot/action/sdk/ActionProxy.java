package com.xlmkit.springboot.action.sdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ActionProxy implements InvocationHandler {
	
	private Client client;
	@SuppressWarnings("unchecked")
	public static <T> T create(Class<T> actionClass,Client client) {
		ActionProxy proxy = new ActionProxy();
		proxy.client = client;
		return (T) Proxy.newProxyInstance(Client.class.getClassLoader(), new Class[] { actionClass }, proxy);
	}
	@SuppressWarnings("unchecked")
	public static <T> T create(Class<T> actionClass) {
		ActionProxy proxy = new ActionProxy();
		return (T) Proxy.newProxyInstance(Client.class.getClassLoader(), new Class[] { actionClass }, proxy);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		Client client = this.client;
		if(client==null) {
			client = (Client) args[0];
		}
		return client.invoke(proxy, method, args);
	}

}
