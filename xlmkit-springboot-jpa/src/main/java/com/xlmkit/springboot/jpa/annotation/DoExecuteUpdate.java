package com.xlmkit.springboot.jpa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @author 小龙码
 * 执行更新操作，返回值请设置成int类型，返回影响条数
 * <pre>
 *
 *
 *      <pre>
 *          UPDATE User SET name=${name} WHERE id=${id}
 *      </pre>
 *
 *     @DoExecuteUpdate
 *     public int updateName(long id,String name);
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface DoExecuteUpdate {
}