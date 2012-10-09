package com.eventbus;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eventbus.context.EventbusContext;

public class AsyncPublishProvider
{
	protected Log logger = LogFactory.getLog(this.getClass());
	protected int poolSize = 2;
	protected ThreadPoolExecutor pool;
	protected EventbusContext context;

	public void init(EventbusContext context)
	{
		this.context = context;
		pool = new ThreadPoolExecutor(poolSize, poolSize, 60, TimeUnit.SECONDS, new LinkedBlockingQueue());
	}

	public void destroy()
	{
		if (pool == null)
			return;
		pool.shutdown();
		int size = pool.getQueue().size();
		if (!pool.isTerminated())
		{
			logger.info("[eventbus] pool wait shutdown, thread quene size " + size);
		}
		while (!pool.isTerminated())
		{
			try
			{
				Thread.sleep(200);
			}
			catch (InterruptedException e)
			{
				break;
			}
		}
		logger.info("[eventbus] pool shutdown success!");
		pool = null;
	}

	public void publish(Subscriber subscriber, Object event)
	{
		pool.execute(new SubscribeRunTarget(subscriber, event));
	}

	public int getPoolSize()
	{
		return poolSize;
	}

	public void setPoolSize(int poolSize)
	{
		this.poolSize = poolSize;
	}
}
