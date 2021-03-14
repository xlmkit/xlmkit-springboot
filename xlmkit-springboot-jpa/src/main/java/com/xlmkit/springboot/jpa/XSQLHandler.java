package com.xlmkit.springboot.jpa;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.xlmkit.springboot.jpa.util.CastUtils;
import org.hibernate.query.NativeQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;

public class XSQLHandler {
	private @PersistenceContext EntityManager defaultEntityManager;

	public Object execute(MethodContext methodContext, Object[] args) throws Exception {
		Class<?> returnType = methodContext.getReturnType();
		Type genericReturnType = methodContext.getGenericReturnType();
		if (Page.class.isAssignableFrom(returnType)) {
			Type itemGenericType = JSONObject.class;
			if (genericReturnType instanceof ParameterizedType) {
				ParameterizedType new_name = (ParameterizedType) genericReturnType;
				itemGenericType = new_name.getActualTypeArguments()[0];
			}
			Pageable pageable = (Pageable) args[args.length - 1];
			QueryContext listQueryContext = methodContext.createQueryContext(QueryType.LIST, args);
			List<Object> list = (List<Object>) listResult(methodContext, itemGenericType, listQueryContext, pageable);

			QueryContext countQueryContext = methodContext.createQueryContext(QueryType.COUNT, args);

			JSONObject countResult = (JSONObject) singleResult(methodContext, JSONObject.class, countQueryContext);
			long total = 0;
			if (countResult.size() == 1) {
				total = ((Number) countResult.values().toArray()[0]).longValue();
			} else {
				total = ((Number) countResult.get("total")).longValue();
			}
			return new PageResult<Object>(list, pageable, total, countResult);
		} else if (List.class.isAssignableFrom(returnType)) {
			Type itemType = JSONObject.class;
			if (genericReturnType instanceof ParameterizedType) {
				ParameterizedType new_name = (ParameterizedType) genericReturnType;
				itemType = new_name.getActualTypeArguments()[0];
			}
			return listResult(methodContext, itemType, methodContext.createQueryContext(QueryType.LIST, args), null);
		} else if (methodContext.isDoExecuteUpdate()) {
			return doUpdate(methodContext, genericReturnType, methodContext.createQueryContext(QueryType.LIST, args));
		} else {
			return singleResult(methodContext, genericReturnType,
					methodContext.createQueryContext(QueryType.LIST, args));
		}
	}

	private int doUpdate(MethodContext methodContext, Type type, QueryContext queryContext) {
		if (type.getTypeName().equals("T")) {
			type = methodContext.getEntityClass();
		}
		EntityManager entityManager = getEntityManager(queryContext.attrs());
		String sql = queryContext.query().toString();
		Query query = createQuery(methodContext, entityManager, type, sql, queryContext);
		return query.executeUpdate();
	}

	private boolean isSimpleClass(Type type) {
		if (Class.class.isAssignableFrom(type.getClass())) {
			return singleClasses.indexOf(type) >= 0;
		}
		return false;
	}

	private Object singleResult(MethodContext methodContext, Type type, QueryContext queryContext) {
		if (type.getTypeName().equals("T")) {
			type = methodContext.getEntityClass();
		}
		EntityManager entityManager = getEntityManager(queryContext.attrs());
		String sql = queryContext.query().toString();
		boolean simpleType = isSimpleClass(type);
		Query query = createQuery(methodContext, entityManager, type, sql, queryContext);
		Object result = null;

		try {
			result = query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
		if (result == null) {
			return null;
		}
		if (simpleType) {
			return CastUtils.cast(result, type);
		} else {
			return TypeUtils.cast(result, type, new ParserConfig(true));
		}
	}

	private List<Object> listResult(MethodContext methodContext, Type type, QueryContext queryContext,
									Pageable pageable) {
		if (type.getTypeName().equals("T")) {
			type = methodContext.getEntityClass();
		}
		EntityManager entityManager = getEntityManager(queryContext.attrs());
		String sql = queryContext.query().toString();
		boolean simpleType = isSimpleClass(type);
		Query query = createQuery(methodContext, entityManager, type, sql, queryContext);
		if (pageable != null) {
			query.setFirstResult((int) pageable.getOffset());
			query.setMaxResults(pageable.getPageSize());
		}
		List<?> resultList = query.getResultList();
		if (simpleType) {
			List<Object> list = new ArrayList<>();
			for (Object item : resultList) {
				list.add(CastUtils.cast(item, type));
			}
			return list;
		} else {
			List<Object> list = new ArrayList<>();
			for (Object item : resultList) {
				list.add(TypeUtils.cast(item, type, new ParserConfig(true)));
			}
			return list;
		}
	}

	private static List<Class<?>> singleClasses = new ArrayList<>();
	static {
		singleClasses.add(BigInteger.class);
		singleClasses.add(String.class);
		singleClasses.add(BigDecimal.class);
		singleClasses.add(Integer.class);
		singleClasses.add(int.class);
		singleClasses.add(Long.class);
		singleClasses.add(long.class);
		singleClasses.add(Boolean.class);
		singleClasses.add(boolean.class);
		singleClasses.add(float.class);
		singleClasses.add(Float.class);
		singleClasses.add(Double.class);
		singleClasses.add(double.class);

		singleClasses.add(Date.class);
	}

	private EntityManager getEntityManager(Map<String, Object> options) {
		return defaultEntityManager;
	}

	@SuppressWarnings("deprecation")
	public Query createQuery(MethodContext methodContext, EntityManager entityManager, Type type, String sql,
							 QueryContext queryContext) {
		javax.persistence.Query query = null;
		while (sql.indexOf('\n') >= 0) {
			sql = sql.replace("\n", "");
		}
		while (sql.indexOf('\r') >= 0) {
			sql = sql.replace("\r", "");
		}
		while (sql.indexOf(" T ") >= 0) {
			sql = sql.replace(" T ", " " + methodContext.getTableName() + " ");
		}
		sql = sql.replaceAll("\\$\\.(\\S{1,})", methodContext.getTablePrefix() + "$1");
		boolean simpleType = isSimpleClass(type);
		if (simpleType) {
			query = entityManager.createNativeQuery(sql);
		} else {
			query = entityManager.createNativeQuery(sql);
			query.unwrap(NativeQuery.class).setResultTransformer(new JSONObjectTransformer());
		}
		for (int i = 0; i < queryContext.placeholders().size(); i++) {
			query.setParameter(i + 1, queryContext.placeholders().get(i));
		}
		if (queryContext.attrs().containsKey("maxResults")) {
			int maxResults = queryContext.attrs().getIntValue("maxResults");
			query.setMaxResults(maxResults);
		}

		return query;
	}

}
