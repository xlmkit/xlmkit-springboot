package com.xlmkit.springboot.jpa;
import com.xlmkit.springboot.jpa.annotation.DoExecuteUpdate;
import com.xlmkit.springboot.jpa.annotation.HQL;
import com.xlmkit.springboot.jpa.annotation.Model;
import com.xlmkit.springboot.jpa.util.MD5;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
@Slf4j
@Data
public class MethodContext {
		private Class<?> entityClass;
		private String entitySuffix = "";
		private String tablePrefix = "";
		private boolean isHQL = false;
		private String scriptMethod;
		private Class<?>[] parameterTypes;
		private String script;
		private Class<?> returnType;
		private Type genericReturnType;
		private String entityName;
		private String tableName;
		private ScriptEngine scriptEngine ;
		private boolean doExecuteUpdate;
		public MethodContext(ScriptEngine scriptEngine, Method method, String script) {

			this.doExecuteUpdate = method.getAnnotation(DoExecuteUpdate.class)!=null;
			this.scriptEngine = scriptEngine;
			entityClass = method.getDeclaringClass().getAnnotation(Model.class).value();
			Entity entity = entityClass.getAnnotation(Entity.class);
			this.entityName = StringUtils.isEmpty(entity.name()) ? entityClass.getSimpleName() : entity.name();
			if (!this.entityName.equals(entityClass.getSimpleName())) {
				entitySuffix = this.entityName.substring(entityClass.getSimpleName().length());
			}
			Table table = entityClass.getAnnotation(Table.class);
			this.tableName = (table != null && !StringUtils.isEmpty(table.name())) ? table.name()
					: entityClass.getSimpleName();
			if (this.tableName.length() > entityClass.getSimpleName().length()) {
				tablePrefix = this.tableName.substring(0,
						this.tableName.length() - entityClass.getSimpleName().length());
			}
			isHQL = method.getAnnotation(HQL.class) != null;
			scriptMethod = "_" + MD5.MD5_32bit(method.toString());
			parameterTypes = method.getParameterTypes();
			returnType = method.getReturnType();
			genericReturnType = method.getGenericReturnType();
			this.script = script;
		}

		public String getTablePrefix() {
			return tablePrefix;
		}

		public String getEntitySuffix() {
			return entitySuffix;
		}

		public String getEntityName() {
			return entityName;
		}

		public String getTableName() {
			return tableName;
		}

		public Class<?> getEntityClass() {
			return entityClass;
		}

		public Class<?> getReturnType() {
			return returnType;
		}

		public Type getGenericReturnType() {
			return genericReturnType;
		}

		public QueryContext createQueryContext(QueryType queryType, Object[] args) {
			Object[] invokeArgs = new Object[args.length + 1];
			Class<?>[] invokeClassess = new Class<?>[args.length + 1];
			QueryContext queryContext = new QueryContext(queryType);
			invokeArgs[0] = queryContext;
			invokeClassess[0] = QueryContext.class;
			for (int i = 0; i < args.length; i++) {
				invokeArgs[i + 1] = args[i];
				invokeClassess[i + 1] = parameterTypes[i];
			}
			try {
				Invocable invocable = (Invocable) scriptEngine;
				invocable.invokeFunction(scriptMethod, invokeArgs);
			} catch (Exception e) {
				int i = 0;
				for (String item : script.split("\n")) {
					log.error(i + " " + item);
					i++;
				}
				throw new RuntimeException(e);
			}
			return queryContext;
		}

	}