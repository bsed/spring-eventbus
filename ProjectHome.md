spring-eventbus is a java library that support eventbus in spring framework. it has some deferent features between GWT eventbus.<br>
1. support asynchronous call. <br>
some long time task(http call) need to run in a new thread <br>
2. support transaction.<br>
if subscriber use asynchronous mode and the publisher is in a transaction, the subscriber will be notified only when transaction has commited.<br>
3 support annotation publish<br>
see the flow publish example.<br>

<h2>config in spring.</h2>
<pre><code>&lt;bean id="eventBus" class="com.eventbus.spring.SpringEventBus" init-method="init" destroy-method="destroy"&gt;
	&lt;!-- set to true if you want to output debug message --&gt;
	&lt;property name="debug" value="true"/&gt;
&lt;/bean&gt;
&lt;!-- if you want to use @publish annotation to publish a message --&gt;
&lt;aop:aspectj-autoproxy/&gt;
</code></pre>
<h2>How To Publish.</h2>
<pre><code>@EventBusService 
class PublishClass()
{
	@Autowired
	private EventBus eventbus;

	public void pubmethodA()
	{
		//publish event with a subject
		eventbus.publish("subjectA", "new event");
		//publish a user defined event without subject
		eventbus.publish(new MyEvent());
	}

	@publish(subject="subjectB")//publish event through annotation
	public void pubmethodB()
	{
		//dosomething
	}
}
</code></pre>
<h2>How To Subscribe</h2>
<pre><code>@EventBusService 
class SubscribeClass()
{
	@subscribe
	public void mySubMethodA(MyEvent event){
	//dosomething
	}

	@subscribe(isAsync=true)
	public void mySubMethodA(MyEvent event){
	//this method will invoke in another thread. if the publisher run in an transaction, 
	//it will be called only if the transaction commit successful.
	}
}
</code></pre>