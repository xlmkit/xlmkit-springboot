package com.xlmkit.springboot.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DaoService {
	private @PersistenceContext EntityManager manager;

	@Transactional(rollbackFor = Throwable.class)
	public <T> T save(T t) {
		manager.persist(t);
		return t;
	}
	public EntityManager getManager() {
		return manager;
	}
}
