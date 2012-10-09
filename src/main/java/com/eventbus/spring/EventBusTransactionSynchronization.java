package com.eventbus.spring;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.transaction.support.TransactionSynchronization;

import com.eventbus.SubscribeRunTarget;
import com.eventbus.Subscriber;

public class EventBusTransactionSynchronization implements TransactionSynchronization
{
	private ThreadPoolExecutor pool;
	private Subscriber subscriber;
	private Object event;

	public EventBusTransactionSynchronization(ThreadPoolExecutor pool, Subscriber subscriber, Object event)
	{
		this.pool = pool;
		this.subscriber = subscriber;
		this.event = event;
	}

	@Override
	public void afterCommit()
	{
		pool.execute(new SubscribeRunTarget(subscriber, event));
	}

	@Override
	public void afterCompletion(int i)
	{
	}

	@Override
	public void beforeCommit(boolean flag)
	{
	}

	@Override
	public void beforeCompletion()
	{
	}

	@Override
	public void resume()
	{
	}

	@Override
	public void suspend()
	{
	}
}
