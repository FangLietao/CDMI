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
import org.jose4j.lang.JoseException;

import com.philips.dacHttp.DacRequestEntity;
import com.philips.dacHttp.DacResponseEntity;
import com.philips.dacHttp.ObjectKey;
import com.philips.dacHttp.SecurityDacRequestEntity;
import com.philips.dacHttp.SecurityDacResponseEntity;
import com.philips.dao.ACLContainerDao;
import com.philips.dao.ACLObjectDao;
import com.philips.model.ACEntity;
import com.philips.model.ACLContainer;
import com.philips.model.ACLObject;
import com.philips.service.AuthorityService;
import com.philips.util.Identifier;
import com.philips.util.MediaTypes;

public class PathResource {
	private ACLObjectDao aclObjectDao;
	private ACLContainerDao aclContainerDao;
	private AuthorityService authorityService;

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

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
	@Path("/")
	@Consumes(MediaTypes.DAC_OBJECT)
	public Response getAccessAuthorition(@Context HttpHeaders header,
			byte[] bytes) {

		SecurityDacRequestEntity securityReqEntity = new SecurityDacRequestEntity(
				new String(bytes));
		String jws = securityReqEntity
				.sigVertifyDacRequestEntity(securityReqEntity.getDac_request());
		String jwe;
		DacRequestEntity reqEntity = null;
		try {
			jwe = securityReqEntity.decryptDacResponseEntity(jws);
			reqEntity = new DacRequestEntity(jwe);
		} catch (JoseException e) {
			e.printStackTrace();			
		} catch (ParseException e) {
			e.printStackTrace();
		}

		DacResponseEntity responseEntity = new DacResponseEntity();
		responseEntity = authorityService.getAccessAuthorition(reqEntity);
		SecurityDacResponseEntity securityDacResponseEntity = new SecurityDacResponseEntity();

		return Response.ok(
				securityDacResponseEntity.getSecurityRequestEntity(
						responseEntity, reqEntity.getServerIdentity())).build();

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
