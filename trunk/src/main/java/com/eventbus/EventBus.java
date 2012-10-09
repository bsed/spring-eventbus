package com.eventbus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eventbus.annotation.Subscribe;
import com.eventbus.context.EventbusContext;

/**
 * 
 * 
 * @author zja
 *
 */
public class EventBus implements EventbusContext
{
	protected Log logger = LogFactory.getLog(this.getClass());

	protected Hashtable<Object, List<Subscriber>> subTargets = new Hashtable<Object, List<Subscriber>>();
	protected AsyncPublishProvider asyncPublishProvider;
	protected boolean debug = logger.isDebugEnabled();

	public void init()
	{
		if (asyncPublishProvider == null)
		{
			asyncPublishProvider = new AsyncPublishProvider();
			asyncPublishProvider.init(this);
		}
	}

	public void addSubscriber(Object obj)
	{
		Method[] methods = obj.getClass().getMethods();
		for (Method m : methods)
		{
			if (m.getAnnotations().length == 0)
				continue;

			Subscribe a = m.getAnnotation(Subscribe.class);
			if (a == null)
				continue;
			Subscriber target = new Subscriber();
			if (a.subject() != null && a.subject().length() != 0)
				target.subject = a.subject();
			target.object = obj;
			target.isAsync = a.isAsyn();
			target.method = m;

			addSubscribe(target);
		}
	}

	public void addSubscribe(Subscriber target)
	{
		Method m = target.method;
		Class[] paramTypes = m.getParameterTypes();
		if (paramTypes.length > 1)
		{
			logger.error("[eventbus] subscriber only support one parameter ");
			return;
		}
		if (paramTypes.length == 0 && target.subject == null)
		{
			logger.error("[eventbus] subscriber should set subject when has no event parameter");
			return;
		}

		Object key = (target.subject != null) ? target.subject : paramTypes[0];
		List<Subscriber> values = subTargets.get(key);
		if (values == null)
		{
			values = (List<Subscriber>) Collections.synchronizedList(new ArrayList<Subscriber>());
			subTargets.put(key, values);
		}
		values.add(target);
		logger.info("[eventbus] add subscriber [" + target.method.toGenericString() + "]");
	}

	protected boolean isJavaClass(Class c)
	{
		return c.isPrimitive() || c.isArray() || c.getName().startsWith("java.");
	}

	protected boolean isPublishArgValid(String arg)
	{
		if ("retVal".equals(arg))
			return true;
		if (arg.startsWith("args[") && arg.endsWith("]"))
			return true;
		return false;
	}

	public void publish(Object customerEvent)
	{
		Class c = customerEvent.getClass();
		if (isJavaClass(c))
		{
			logger.error("[eventbus] publish should set subject when event isn't customer class");
			return;
		}
		if (debug)
			logger.info("publish an event " + customerEvent.toString());
		boolean success = false;
		for (Class tc = c; !isJavaClass(tc); tc = tc.getSuperclass())
		{
			List<Subscriber> subscriberList = subTargets.get(tc);
			if (subscriberList == null || subscriberList.size() == 0)
				continue;
			for (Subscriber subscriber : subscriberList)
				doPub(subscriber, customerEvent);
			success = true;
		}
		if (!success)
			logger.warn("[eventbus] cant' find any subscriber with event type " + c.getName());
	}

	public void publish(String subject, Object event)
	{
		if (subject == null || subject.trim().length() == 0)
		{
			logger.error("[eventbus] wrong parameter when call publish(String subject, Object event)!");
			return;
		}
		List<Subscriber> subscriberList = subTargets.get(subject);
		if (subscriberList == null || subscriberList.size() == 0)
		{
			logger.warn("[eventbus] cant' find any subscriber with event type " + subject);
			return;
		}
		for (Subscriber subscriber : subscriberList)
			doPub(subscriber, event);
	}

	protected void doPub(Subscriber subscriber, Object event)
	{
		if (subscriber.isAsync)
		{
			if (debug)
				logger.info("subscriber async " + subscriber.method.toString() + ",event " + event.toString());
			asyncPublishProvider.publish(subscriber, event);
			return;
		}

		try
		{
			if (debug)
				logger.info("subscriber call " + subscriber.method.toString() + ",event " + event.toString());

			subscriber.invoke(event);
		}
		catch (Exception e)
		{
			logger.error("[eventbus] invoke subscriber fail ", e);
		}
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	public boolean isDebug()
	{
		return debug;
	}

	public void destroy()
	{
		if (asyncPublishProvider != null)
			asyncPublishProvider.destroy();
		logger.info("eventbus shutdown success!");
	}
}
