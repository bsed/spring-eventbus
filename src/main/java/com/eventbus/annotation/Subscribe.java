package com.eventbus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(
{ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe
{
	public String subject() default "";

	public boolean isAsyn() default false;
}
