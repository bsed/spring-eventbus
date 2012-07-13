package com.eventbus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SubscribeRunTarget implements Runnable
{
	private static Log logger = LogFactory.getLog(SubscribeRunTarget.class);
	Subscriber subscriber;
	Object event;

	public SubscribeRunTarget(Subscriber subscriber, Object event)
	{
		this.subscriber = subscriber;
		this.event = event;
	}

	@Override
	public void run()
	{
		try
		{
			subscriber.invoke(event);
		}
		catch (Exception e)
		{
			logger.error("[eventbus] invoke subscriber fail ", e);
		}
	}
}
