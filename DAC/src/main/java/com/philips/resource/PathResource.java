package com.philips.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import net.minidev.json.parser.ParseException;

import org.codehaus.jackson.JsonParseException;
import org.jose4j.jwk.JsonWebKey;

import com.philips.dacHttp.DacRequestEntity;
import com.philips.dacHttp.DacResponseEntity;
import com.philips.dacHttp.ObjectKey;
import com.philips.dao.ACLContainerDao;
import com.philips.dao.ACLObjectDao;
import com.philips.model.ACEntity;
import com.philips.model.ACLContainer;
import com.philips.model.ACLObject;
import com.philips.util.Identifier;
import com.philips.util.MediaTypes;

public class PathResource {
	ACLObjectDao aclObjectDao;
	ACLContainerDao aclContainerDao;

	public ACLContainerDao getAclContainerDao() {
		return aclContainerDao;
	}

	public void setAclContainerDao(ACLContainerDao aclContainerDao) {
		this.aclContainerDao = aclContainerDao;
	}

	public ACLObjectDao getAclObjectDao() {
		return aclObjectDao;
	}

	public void setAclObjectDao(ACLObjectDao aclObjectDao) {
		this.aclObjectDao = aclObjectDao;
	}

	@POST
	@Path("/{path:.+}/")
	@Consumes(MediaTypes.DAC_OBJECT)
	public Response getAccessAuthorition(@PathParam("path") String path,
			@Context HttpHeaders header, byte[] bytes) {
		DacResponseEntity responseEntity = new DacResponseEntity();
		try {
			DacRequestEntity reqEntity = new DacRequestEntity(bytes);
			// find the acl object by cdmi object id
			ACLObject aclObj = aclObjectDao.getACL(reqEntity.getCdmiObjectId());
			
			if (aclObj == null) {
				// if the acl not exist,create a defalut acl of the obj
				aclObj = new ACLObject();
				aclObj.setObjID(reqEntity.getCdmiObjectId());
				ACEntity aclEntity = new ACEntity();
				aclEntity.setIdentifier("EVERYONE@");
				aclEntity.setAceType("DENY");
				aclEntity.setAceFlags("0x00000000");
				aclEntity.setAceMask("0x00000000");
				List<ACEntity> objACL = new ArrayList<ACEntity>();
				objACL.add(aclEntity);
				aclObj.setObjACL(objACL);

				aclObjectDao.createACL(reqEntity.getCdmiObjectId(), aclObj);

				responseEntity.withDacAppliedMask(aclEntity.getAceMask());
			} else {
				List<ACEntity> acl = aclObj.getObjACL();
				int i;
				for (i = 0; Identifier.transToIdentifier(((ACEntity) acl.get(i)).getIdentifier() )<Identifier.transToIdentifier( reqEntity
						.getClientIdentity().get("acl_group"))&&i<acl.size(); i++) {
				}
				if(i==acl.size()){
					ACEntity aclEntity = new ACEntity();
					aclEntity.setIdentifier("EVERYONE@");
					aclEntity.setAceType("DENY");
					aclEntity.setAceFlags("0x00000000");
					aclEntity.setAceMask("0x00000000");
					acl.add(aclEntity);					
				}
				responseEntity.withDacAppliedMask(((ACEntity) acl.get(i))
						.getAceMask());
			}

			JsonWebKey objKey = ObjectKey.getObjectKey((reqEntity.getCdmiEncKeyId()));
			responseEntity.withDacObjectKey(objKey).withDacResponseId(UUID.randomUUID().toString());

		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		
		String s=responseEntity.getJSONresponseEntity();

		return Response.ok(responseEntity.getJSONresponseEntity()).build();

	}

	/*
	 * Operation about container(create, delete)
	 * 
	 * @param path Path to the new or existing container
	 * 
	 * @param headers HttpHeaders of the container operation request
	 * 
	 * @param bytes Entities of the container operation request
	 */
	@PUT
	@Path("/{path:.+}/")
	@Consumes(MediaTypes.DAC_CONTAINER)
	public Response createContainer(@PathParam("path") String path,
			@Context HttpHeaders headers, byte[] bytes) {
		ACLContainer aclContainer = aclContainerDao.getDacContainer(path);
		if (aclContainer != null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.tag("the container existing").build();
		}
		try {
			aclContainer = new ACLContainer();
			aclContainer.fromJSON(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		aclContainerDao.createDacContainer(path, aclContainer);

		return Response.ok().build();

	}

	@DELETE
	@Path("/{path:.+}/")
	@Consumes(MediaTypes.DAC_CONTAINER)
	public Response deleteAclContainer(@PathParam("path") String path) {
		ACLContainer aclContainer = aclContainerDao.getDacContainer(path);
		if (aclContainer == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.tag("container not existing").build();
		}
		aclContainerDao.deleteDacContainer(path);
		return Response.status(Response.Status.NO_CONTENT).build();

	}

	/*
	 * Operation about Object(create, get, delete and access control list
	 * operation)
	 * 
	 * @param path Path to the new or existing object
	 * 
	 * @param headers HttpHeaders of the container operation request
	 * 
	 * @param bytes Entities of the container operation request
	 */
	@PUT
	@Path("/{path:.+}")
	@Consumes(MediaTypes.DAC_OBJECT)
	public Response createACLObject(@PathParam("path") String path,
			@Context HttpHeaders headers, byte[] bytes) {
		ACLObject aclObject = aclObjectDao.getACL(path);
		if (aclObject != null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.tag("the object existing").build();
		}

		try {
			ACLObject aclObj = new ACLObject();
			aclObj.fromJSON(bytes);
			aclObjectDao.createACL(path, aclObj);

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Response.ok().build();
	}

	@GET
	@Path("/{path:.+}")
	public Response getACLObject(@PathParam("path") String path,
			@Context HttpHeaders headers, byte[] bytes) {
		ACLObject aclObj = new ACLObject();
		aclObj = aclObjectDao.getACL(path);
		if (aclObj == null) {
			// return default access control
			return Response.status(Response.Status.NOT_FOUND).build();
		} else {		

			return Response.ok(aclObj.toJSON()).build();
		}

	}

	@DELETE
	@Path("/{path:.+}/")
	@Consumes(MediaTypes.DAC_OBJECT)
	public Response deleteAclObject(@PathParam("path") String path,
			@Context HttpHeaders headers) {
		ACLObject aclObj = new ACLObject();
		aclObj = aclObjectDao.getACL(path);
		if (aclObj == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.tag("aclObject not existing").build();
		}
		aclObjectDao.deleteACL(path);
		return Response.status(Response.Status.NO_CONTENT).build();

	}
}