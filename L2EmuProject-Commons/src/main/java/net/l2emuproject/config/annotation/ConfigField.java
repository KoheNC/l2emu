package net.l2emuproject.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.l2emuproject.config.converters.Converter;
import net.l2emuproject.config.converters.DefaultConverter;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigField
{
	public String name();
	
	public String value();
	
	public String[] comment() default {};
	
	public boolean eternal() default false;
	
	public Class<? extends Converter> converter() default DefaultConverter.class;
}
