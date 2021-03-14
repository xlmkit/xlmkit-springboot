/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.xlmkit.springboot.jpa;

import org.hibernate.transform.AliasedTupleSubsetResultTransformer;

import com.alibaba.fastjson.JSONObject;

public class JSONObjectTransformer extends AliasedTupleSubsetResultTransformer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isTransformedValueATupleElement(String[] aliases, int tupleLength) {
		return false;
	}

	@Override
	public Object transformTuple(Object[] tuple, String[] aliases) {
		JSONObject jsonObject = new JSONObject();
		for (int i = 0; i < aliases.length; i++) {
			jsonObject.put(aliases[i], tuple[i]);
		}
		return jsonObject;
	}

}
