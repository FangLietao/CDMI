package com.philips.dao;

import com.philips.model.ACLContainer;

public interface ACLContainerDao {
	public void createDacContainer(String path, ACLContainer container);

	public void deleteDacContainer(String path);

	public ACLContainer getDacContainer(String path);
}
