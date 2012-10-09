package com.eventbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Subscriber
{
	public Object object;
	public Method method;
	public String subject;
	public boolean isAsync;

	public void invoke(Object event) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Method m = this.method;
		Class[] paramTypes = m.getParameterTypes();
		if (paramTypes.length == 0)
			m.invoke(this.object);
		else
			m.invoke(this.object, event);
	}
}
