
package com.xlmkit.springboot.action;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableActionRegistrarSupport.class)
@Repeatable(EnableActions.class)
public @interface EnableAction {
	Class<?>[] value() default {};
	String separator() default "/";
	String prefix()  default "";
}
