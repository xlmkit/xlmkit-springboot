
package com.xlmkit.springboot.jpa;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
/**
 *
 *
 * @author 小龙码
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableCommentDaoRegistrarSupport.class)
@Repeatable(EnableCommentDaos.class)
public @interface EnableCommentDao {
	Class<?>[] value() default {};
	String tablePrefix() default "";
	
}
