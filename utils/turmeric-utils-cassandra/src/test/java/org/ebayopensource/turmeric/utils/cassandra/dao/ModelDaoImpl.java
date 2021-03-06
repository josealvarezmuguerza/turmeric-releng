/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.utils.cassandra.dao;

import java.util.List;
import java.util.Set;

import org.ebayopensource.turmeric.utils.cassandra.model.Model;

/**
 * The Class ModelDaoImpl.
 * @author jamuguerza
 */
public class ModelDaoImpl<K> extends
		AbstractColumnFamilyDao<K, Model> implements ModelDao<K>{

	/**
	 * Instantiates a new model dao impl.
	 *
	 * @param clusterName the cluster name
	 * @param host the host
	 * @param keySpace the key space
	 * @param cf the cf
	 */
	public ModelDaoImpl(final String clusterName,  final String host, final String keySpace, final String cf,  final Class<K> kTypeClass) {
		super(clusterName, host, keySpace, kTypeClass, Model.class,  cf);
	}

	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.utils.cassandra.dao.ModelDao#save(org.ebayopensource.turmeric.utils.cassandra.model.Model)
	 */
	public void save(final Model<?> testModel) {
		super.save((K)testModel.getKey(), testModel);
	}

	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.utils.cassandra.dao.ModelDao#delete(org.ebayopensource.turmeric.utils.cassandra.model.Model)
	 */
	public void delete(final Model<?> testModel) {
		super.delete((K)testModel.getKey());
	}
	

	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.utils.cassandra.dao.ModelDao#getAllKeys()
	 */
	@Override
	public Set<K> getAllKeys() {
		return (Set<K>) super.getKeys();
	}
	
	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.utils.cassandra.dao.AbstractColumnFamilyDao#containsKey(java.lang.Object)
	 */
	public boolean containsKey(final K key){
		return super.containsKey((K)key);
	}
	
	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.utils.cassandra.dao.AbstractColumnFamilyDao#findItems(java.util.List, java.lang.String, java.lang.String)
	 */
	public Set<Model> findItems(final List<K> keys, final String rangeFrom, final String rangeTo ) {
		return super.findItems(keys, rangeFrom, rangeTo);
	}

	


	
	
}