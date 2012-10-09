package com.eventbus.spring;

import com.eventbus.context.EventbusContext;

public class SpringEventbusContext
{
	private static EventbusContext context;

	public static EventbusContext getContext()
	{
		return context;
	}

	public static void setContext(EventbusContext context)
	{
		SpringEventbusContext.context = context;
	}

}
