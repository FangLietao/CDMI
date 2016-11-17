package com.philips.dao;

import com.philips.model.ACLObject;

public interface ACLObjectDao {
	public void createACL(String path, ACLObject obj);
	
	public void deleteACL(String path);

	public ACLObject getACL(String path);

	public ACLObject updateACL(ACLObject obj, ACLObject dobj);
}
