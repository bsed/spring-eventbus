spring-eventbus is a java library that support eventbus in spring framework. it has some deferent features between GWT eventbus.<br>
1. support asynchronous call. <br>
some long time task(http call) need to run in a new thread <br>
2. support transaction.<br>
if subscriber use asynchronous mode and the publisher is in a transaction, the subscriber will be notified only when transaction has commited.<br>
3 support annotation publish<br>
see the flow publish example.<br>

<h2>config in spring.</h2>
<pre><code>&lt;bean id="eventBus" class="com.eventbus.spring.SpringEventBus" init-method="init" destroy-method="destroy"&gt;<br>
	&lt;!-- set to true if you want to output debug message --&gt;<br>
	&lt;property name="debug" value="true"/&gt;<br>
&lt;/bean&gt;<br>
&lt;!-- if you want to use @publish annotation to publish a message --&gt;<br>
&lt;aop:aspectj-autoproxy/&gt;<br>
</code></pre>
<h2>How To Publish.</h2>
<pre><code>@EventBusService <br>
class PublishClass()<br>
{<br>
	@Autowired<br>
	private EventBus eventbus;<br>
<br>
	public void pubmethodA()<br>
	{<br>
		//publish event with a subject<br>
		eventbus.publish("subjectA", "new event");<br>
		//publish a user defined event without subject<br>
		eventbus.publish(new MyEvent());<br>
	}<br>
<br>
	@publish(subject="subjectB")//publish event through annotation<br>
	public void pubmethodB()<br>
	{<br>
		//dosomething<br>
	}<br>
}<br>
</code></pre>
<h2>How To Subscribe</h2>
<pre><code>@EventBusService <br>
class SubscribeClass()<br>
{<br>
	@subscribe<br>
	public void mySubMethodA(MyEvent event){<br>
	//dosomething<br>
	}<br>
<br>
	@subscribe(isAsync=true)<br>
	public void mySubMethodA(MyEvent event){<br>
	//this method will invoke in another thread. if the publisher run in an transaction, <br>
	//it will be called only if the transaction commit successful.<br>
	}<br>
}<br>
</code></pre>