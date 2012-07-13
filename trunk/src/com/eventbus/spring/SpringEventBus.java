package com.eventbus.spring;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import ognl.Ognl;
import ognl.OgnlException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.eventbus.EventBus;
import com.eventbus.Subscriber;
import com.eventbus.annotation.EventBusService;
import com.eventbus.annotation.Publish;
import com.eventbus.annotation.Subscribe;

@SuppressWarnings("unchecked")
@Aspect
public class SpringEventBus extends EventBus implements BeanPostProcessor, ApplicationContextAware
{
	private static final String PUB_ANNOATION_EXP = "@annotation(com.eventbus.annotation.Publish)&&@annotation(pub)";
	private ApplicationContext context;

	//	private DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(new AnnotationMatchingPointcut(
	//			EventBusService.class, Publish.class), this);

	@Override
	public void init()
	{
		this.asyncPublishProvider = new SpringAsyncPublishProvider();
		this.asyncPublishProvider.init(this);
		super.init();
	}

	@Override
	public Object postProcessBeforeInitialization(Object obj, String s) throws BeansException
	{
		return obj;
	}

	@Override
	public Object postProcessAfterInitialization(Object obj, String s) throws BeansException
	{
		Class c = obj.getClass();
		if (AopUtils.isAopProxy(obj))
			c = AopUtils.getTargetClass(obj);
		if (c.isAnnotationPresent(EventBusService.class))
		{
			boolean isSingleton = context.containsBean(s) && context.isSingleton(s);
			if (!isSingleton)
			{
				logger.error("[eventbus] prototype bean is not supported!");
				return obj;
			}
			Method[] methods = c.getMethods();
			for (Method m : methods)
			{
				Subscribe sub = m.getAnnotation(Subscribe.class);
				if (sub != null)
				{
					Subscriber target = new Subscriber();
					if (sub.subject() != null && sub.subject().length() != 0)
						target.subject = sub.subject();
					target.object = obj;
					target.isAsync = sub.isAsyn();
					target.method = m;

					addSubscribe(target);
				}
			}
		}
		//			ProxyFactory weaver = new ProxyFactory(obj);
		//			//			weaver.setTargetClass(obj.getClass());
		//			weaver.setProxyTargetClass(true);
		//			weaver.addAdvisor(advisor);
		//			Object proxyObj = weaver.getProxy();
		//			return proxyObj;
		return obj;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationcontext) throws BeansException
	{
		context = applicationcontext;
	}

	@AfterReturning(pointcut = PUB_ANNOATION_EXP, returning = "retVal")
	public void afterReturning(JoinPoint joinPoint, Object retVal, Publish pub)
	{
		Object customerEvent = null;
		try
		{
			if (pub.arg() != null && pub.arg().trim().length() > 0)
			{
				Map map = new HashMap();
				map.put("args", joinPoint.getArgs());
				map.put("retVal", retVal);
				if (!isPublishArgValid(pub.arg()))
				{
					logger.error("invalid arg expression! " + pub.arg());
					return;
				}
				customerEvent = Ognl.getValue(pub.arg(), map, map);
			}
		}
		catch (OgnlException e)
		{
			logger.error("invalid arg expression! " + pub.arg(), e);
			return;
		}
		joinPoint.getArgs();
		if (pub.subject() == null || pub.subject().trim().length() == 0)
			publish(customerEvent);
		else
			publish(pub.subject(), customerEvent);
	}
}
