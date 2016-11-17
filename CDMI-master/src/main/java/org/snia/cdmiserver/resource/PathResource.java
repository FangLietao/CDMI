/*
 * Copyright (c) 2010, Sun Microsystems, Inc.
 * Copyright (c) 2010, The Storage Networking Industry Association.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of The Storage Networking Industry Association (SNIA) nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 *  THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.snia.cdmiserver.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant;
import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.jose4j.jwk.JsonWebKey;
import org.snia.cdmiserver.dacHttp.CdmiOperation;
import org.snia.cdmiserver.dao.ContainerDao;
import org.snia.cdmiserver.dao.DataObjectDao;
import org.snia.cdmiserver.exception.BadRequestException;
import org.snia.cdmiserver.exception.ForbiddenException;
import org.snia.cdmiserver.model.Container;
import org.snia.cdmiserver.model.DataObject;
import org.snia.cdmiserver.dacHttp.DacRequest;
import org.snia.cdmiserver.dacHttp.DacRequestEntity;
import org.snia.cdmiserver.dacHttp.DacResponseEntity;
import org.snia.cdmiserver.dacHttp.Request;
import org.snia.cdmiserver.util.ACLIdentifier;
import org.snia.cdmiserver.util.MediaTypes;
import org.snia.cdmiserver.util.ObjectID;
import org.snia.fakekms.KMS;

/**
 * <p>
 * Access to objects by path.
 * </p>
 */
public class PathResource {

	private static final Logger LOG = LoggerFactory
			.getLogger(PathResource.class);

	//
	// Properties and Dependency Injection Methods
	//
	private ContainerDao containerDao;

	/**
	 * <p>
	 * Injected {@link ContainerDao} instance.
	 * </p>
	 */
	public void setContainerDao(ContainerDao containerDao) {
		this.containerDao = containerDao;
	}

	private DataObjectDao dataObjectDao;

	/**
	 * <p>
	 * Injected {@link DataObjectDao} instance.
	 * </p>
	 */
	public void setDataObjectDao(DataObjectDao dataObjectDao) {
		this.dataObjectDao = dataObjectDao;
	}

	//
	// Resource Methods
	//
	/**
	 * <p>
	 * [8.8] Delete a Data Object and [9.7] Delete a Container Object
	 * </p>
	 * 
	 * @param path
	 *            Path to the existing object
	 */
	@DELETE
	@Path("/{path:.+}")
	public Response deleteDataObjectOrContainer(@PathParam("path") String path,
			@Context HttpHeaders headers) {
		try {
			DataObject dObj = dataObjectDao.findByPath(path);

			// dac vs acl
			if (headers.getRequestHeader("cdmi_dac_certificate") != null
					&& headers.getRequestHeader("cdmi_dac_uri") != null) {

				// dac
				String identifier;
				String user = headers.getRequestHeader("user").get(0);
				if (user == null) {
					identifier = ACLIdentifier.ANONYMOUS;
				} else if (user.equals(dObj.getMetadata().get("cdmiOwner"))) {
					identifier = ACLIdentifier.OWNER;
				} else {
					identifier = ACLIdentifier.AUTHENTICATED;
				}
				Map<String, String> clientIdentity = new HashMap<String, String>();
				clientIdentity.put("acl_name", user);
				clientIdentity.put("acl_group", identifier);

				DacRequest dac = new DacRequest();
				DacRequestEntity reqEntity = new DacRequestEntity();
				reqEntity
						.withCdmiObjectId(dObj.getObjectID())
						.withDacRequestId(UUID.randomUUID().toString())
						.withAclEffectiveMask(
								CdmiOperation.Opertion.cdmiDelete.toString())
						.withClientIdentity(clientIdentity);

				DacResponseEntity resEntity = dac.operation(
						Request.Method.POST, reqEntity.getJSONDacReqEntity());
				String dacAppliedMask = resEntity.getDacAppliedMask();
				if ((Integer.parseInt(dacAppliedMask.substring(2,
						dacAppliedMask.length() - 1), 16) & 0x00000040) != 0x00000040) {
					return Response.status(Response.Status.FORBIDDEN)
							.tag("no authority").build();
				}

			} else {
				// acl
				// acl have not be achieved
				String appliedMask = dObj.getMetadata().get("cdmi_acl");
				System.out.println(appliedMask + "cdmi_acl");
			}

			containerDao.deleteByPath(path);
			return Response.ok()
					.header("X-CDMI-Specification-Version", "1.0.2").build();
		} catch (Exception ex) {
			LOG.error("Delete error", ex);
			return Response.status(Response.Status.BAD_REQUEST)
					.tag("Object Delete Error : " + ex.toString()).build();
		}
	}

	/**
	 * <p>
	 * Trap to catch attempts to delete the root container
	 * </p>
	 * 
	 * @param path
	 *            Path to the existing data object
	 */
	@DELETE
	@Path("/")
	public Response deleteRootContainer(@PathParam("path") String path) {
		return Response.status(Response.Status.BAD_REQUEST)
				.tag("Can not delete root container").build();
	}

	/**
	 * <p>
	 * [9.4] Read a Container Object (CDMI Content Type) [8.4] Read a Data
	 * Object (CDMI Content Type)
	 * </p>
	 * 
	 * @param path
	 *            Path to the existing non-root container
	 */
	@GET
	@Path("/{path:.+}")
	@Consumes({ MediaTypes.DATA_OBJECT, MediaTypes.CONTAINER })
	@Produces({ MediaTypes.DATA_OBJECT, MediaTypes.CONTAINER })
	public Response getContainerOrDataObjectCDMI(
			@PathParam("path") String path, @Context HttpHeaders headers) {

		LOG.trace("In PathResource.getContainerOrObject, path={}", path);

		// print headers for debug
		if (LOG.isDebugEnabled()) {
			for (String hdr : headers.getRequestHeaders().keySet()) {
				LOG.debug("Hdr: {} - {}", hdr, headers.getRequestHeader(hdr));
			}
		}

		// check if it is not a HTTP method by checking content_type and accept
		// headers
		if (!(headers.getRequestHeader(HttpHeaders.CONTENT_TYPE).contains(
				MediaTypes.DATA_OBJECT)
				|| headers.getRequestHeader(HttpHeaders.CONTENT_TYPE).contains(
						MediaTypes.CONTAINER)
				|| headers.getRequestHeader(HttpHeaders.ACCEPT).contains(
						MediaTypes.DATA_OBJECT) || headers.getRequestHeader(
				HttpHeaders.ACCEPT).contains(MediaTypes.CONTAINER))) {
			return getContainerOrDataObjectNonCDMI(path, headers);
		}

		// Check for container vs object
		if (containerDao.isContainer(path)) {
			// if container build container browser page
			try {
				Container container = containerDao.findByPath(path);
				if (container == null) {
					return Response.status(Response.Status.NOT_FOUND).build();
				} else {
					String respStr = container.toJson(false);
					return Response.ok(respStr).type(MediaTypes.CONTAINER)
							.header("X-CDMI-Specification-Version", "1.0.2")
							.build();
				}
			} catch (Exception ex) {
				LOG.error("Failed to find container", ex);
				return Response.status(Response.Status.NOT_FOUND)
						.tag("Container Read Error : " + ex.toString()).build();
			}
		}

		// get an object
		try {
			DataObject dObj = dataObjectDao.findByPath(path);
			if (dObj == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			} else {

				if (headers.getRequestHeader("cdmi_dac_certificate") != null
						&& headers.getRequestHeader("cdmi_dac_uri") != null) {
					// dac
					String identifier;
					if (headers.getRequestHeader("user").get(0) == null) {
						identifier = ACLIdentifier.ANONYMOUS;
					} else if (headers.getRequestHeader("user").get(0)
							.equals(dObj.getMetadata().get("cdmiOwner"))) {
						identifier = ACLIdentifier.OWNER;
					} else {
						identifier = ACLIdentifier.AUTHENTICATED;
					}
					Map<String, String> clientIdentity = new HashMap<String, String>();
					clientIdentity.put("acl_group", identifier);
					clientIdentity.put("acl_name",
							headers.getRequestHeader("user").get(0));

					DacRequest dac = new DacRequest();
					DacRequestEntity reqEntity = new DacRequestEntity();
					reqEntity
							.withCdmiObjectId(dObj.getObjectID())
							.withDacRequestId(UUID.randomUUID().toString())
							.withAclEffectiveMask(
									CdmiOperation.Opertion.cdmiRead.toString())
							.withClientIdentity(clientIdentity);

					DacResponseEntity resEntity = dac.operation(
							Request.Method.POST,
							reqEntity.getJSONDacReqEntity());
					String dacAppliedMask = resEntity.getDacAppliedMask();
					if ((Integer.parseInt(dacAppliedMask.substring(2,
							dacAppliedMask.length() - 1), 16) & 0x00000001) != 0x00000001) {
						return Response.status(Response.Status.FORBIDDEN)
								.tag("no authority").build();
					}
				} else {
					// acl
					// acl have not be achieved
					String appliedMask = dObj.getMetadata().get("cdmi_acl");
					System.out.println(appliedMask + "cdmi_acl");
				}

				// make http response
				// build a JSON representation
				String respStr = dObj.toJson();
				// ResponseBuilder builder =
				// Response.status(Response.Status.CREATED)
				return Response.ok(respStr)
						.header("X-CDMI-Specification-Version", "1.0.2")
						.type(MediaTypes.DATA_OBJECT).build();
			} // if/else
		} catch (Exception ex) {
			LOG.error("Failed to find data object", ex);
			return Response.status(Response.Status.BAD_REQUEST)
					.tag("Object Fetch Error : " + ex.toString()).build();
		}
	}

	/**
	 * <p>
	 * [9.4] Read a Container Object (CDMI Content Type). Catches request
	 * routing for root container in spite of CXF bug.
	 * </p>
	 * 
	 * @param path
	 *            Path to the root container
	 */
	@GET
	@Path("/")
	@Consumes(MediaTypes.CONTAINER)
	@Produces(MediaTypes.CONTAINER)
	public Response getRootContainer(@PathParam("path") String path,
			@Context HttpHeaders headers) {

		LOG.trace("In PathResource.getRootContainer");
		return getContainerOrDataObjectCDMI(path, headers);

	}

	/**
	 * <p>
	 * [8.5] Read a Data Object (Non-CDMI Content Type) [9.5] Read a Container
	 * Object (Non-CDMI Content Type)
	 * </p>
	 * 
	 * <p>
	 * IMPLEMENTATION NOTE - Consult <code>uriInfo.getQueryParameters()</code>
	 * to identify restrictions on the returned information.
	 * </p>
	 * 
	 * <p>
	 * IMPLEMENTATION NOTE - If the path points at a container, the response
	 * content type must be"text/json".
	 * </p>
	 * 
	 * @param path
	 *            Path to the existing data object or container
	 * @param range
	 *            Range header value (if specified), else empty string
	 */
	@GET
	@Path("/{path:.+}")
	public Response getContainerOrDataObjectNonCDMI(
			@PathParam("path") String path, @Context HttpHeaders headers) {

		LOG.trace("In PathResource.getDataObjectOrContainer, path: {}", path);

		boolean NonCDMI = true;

		// print headers for debug
		for (String hdr : headers.getRequestHeaders().keySet()) {
			if (hdr.equals("x-cdmi-specification-version")) {
				NonCDMI = false;
			}
			LOG.debug("Hdr: {} - {}", hdr, headers.getRequestHeader(hdr));
		}
		if (path == null && NonCDMI) {
			path = new String("/index.html");
		}
		// Check for container vs object
		if (containerDao.isContainer(path)) {
			// if container build container browser page
			try {
				Container container = containerDao.findByPath(path);
				if (container == null) {
					return Response.status(Response.Status.NOT_FOUND).build();
				} else {
					String respStr = container.toJson(false);
					return Response.ok(respStr)
							.header("X-CDMI-Specification-Version", "1.0.2")
							.build();
				}
			} catch (Exception ex) {
				LOG.error("Failed to find container", ex);
				return Response.status(Response.Status.NOT_FOUND)
						.tag("Container Read Error : " + ex.toString()).build();
			}
		} else { // this is an object
			try {
				DataObject dObj = dataObjectDao.findByPath(path);

				if (dObj == null) {
					return Response.status(Response.Status.NOT_FOUND).build();
				}

				JsonWebKey jwk = null;
				if (headers.getRequestHeader("cdmi_dac_certificate") != null
						&& headers.getRequestHeader("cdmi_dac_uri") != null) {
					// dac
					String identifier;
					if (headers.getRequestHeader("user").get(0) == null) {
						identifier = ACLIdentifier.ANONYMOUS;
					} else if (headers.getRequestHeader("user").get(0)
							.equals(dObj.getMetadata().get("cdmiOwner"))) {
						identifier = ACLIdentifier.OWNER;
					} else {
						identifier = ACLIdentifier.AUTHENTICATED;
					}
					Map<String, String> clientIdentity = new HashMap<String, String>();
					clientIdentity.put("acl_group", identifier);
					clientIdentity.put("acl_name",
							headers.getRequestHeader("user").get(0));

					DacRequest dac = new DacRequest();
					DacRequestEntity reqEntity = new DacRequestEntity();
					reqEntity
							.withCdmiObjectId(dObj.getObjectID())
							.withDacRequestId(UUID.randomUUID().toString())
							.withAclEffectiveMask(
									CdmiOperation.Opertion.cdmiRead.toString())
							.withClientIdentity(clientIdentity);
					if (dObj.getMimetype().equals(MediaTypes.JOSEJSON)) {
						reqEntity.withCdmiEncKeyId((String) dObj.getMetadata()
								.get("kid"));
					}

					DacResponseEntity resEntity = dac.operation(
							Request.Method.POST,
							reqEntity.getJSONDacReqEntity());
					String dacAppliedMask = resEntity.getDacAppliedMask();
					if ((Integer.parseInt(dacAppliedMask.substring(2,
							dacAppliedMask.length() - 1), 16) & 0x00000001) != 0x00000001) {
						return Response.status(Response.Status.FORBIDDEN)
								.tag("no authority").build();
					}
					jwk = resEntity.getDacObjectKey();
				} else {
					// acl
					// acl have not be achieved
					String appliedMask = dObj.getMetadata().get("cdmi_acl");
					System.out.println(appliedMask + "cdmi_acl");
				}

				List<MediaType> typeList = headers.getAcceptableMediaTypes();
				String respStr = null;

				// if requested document is an encrypted document
				boolean encrypted;
				switch (dObj.getMimetype()) {
				case MediaTypes.JOSEJSON:
					// encrypted as JOSE in JSON serialization
					encrypted = true;
					try {
						// Do actual decryption
						if (jwk != null) {
							dObj.decryptDataFomJOSEJSON(jwk);
						}else{
							throw new ForbiddenException("no key");
						}

					} catch (BadRequestException ex) {
						// cannot decrypt, internal error
						LOG.error("Could not decrypt ciphertext", ex);
						return Response.status(
								Response.Status.INTERNAL_SERVER_ERROR).build();
					} catch (ForbiddenException ex) {
						// cannot decrypt, no key available
						if (typeList.contains(new MediaType("Application",
								"jose+json"))) {
							// We are allowed to return the ciphertext
							respStr = dObj.getValue();
							return Response.ok(respStr)
									.type(dObj.getMimetype()).build();
						} else {
							// We are not allowed to return the ciphertext: 401
							// error.
							return Response
									.status(Response.Status.UNAUTHORIZED)
									.build();
						}
					}
					break;
				default:
					encrypted = false;
					break;
				}

				// At this point we have a decrypted object in memory.
				// If encrypted is true, then there is also an original
				// ciphertext available somewhere.
				// Check if the resulting mimetype is accepted
				String[] mimestring = dObj.getMimetype().split("/");
				MediaType mime = new MediaType(mimestring[0], mimestring[1]);

				for (MediaType temp : typeList) {

					if (temp.isCompatible(mime)) {
						if ("base64".equals(dObj.getValueTransferEncoding())) {
							return Response
									.ok(Base64.decodeBase64(dObj.getValue()))
									.type(dObj.getMimetype()).build();
						} else {
							respStr = dObj.getValue();
							return Response.ok(respStr)
									.type(dObj.getMimetype()).build();
						}
					}
				}

				// object does not have an acceptable mimetype
				// return encrypted object if possible
				if (encrypted
						&& typeList.contains(new MediaType("Application",
								"jose+json"))) {
					// roll back to encrypted object
					dObj = dataObjectDao.findByPath(path);
					respStr = dObj.getValue();
					return Response.ok(respStr).type(dObj.getMimetype())
							.build();
				} else {
					Variant.VariantListBuilder vlb = Variant.VariantListBuilder
							.newInstance();
					vlb.mediaTypes(mime, MediaType.WILDCARD_TYPE);
					return Response.notAcceptable(vlb.build()).build();
				}

			} catch (Exception ex) {
				LOG.error("Failed to find data object", ex);
				return Response.status(Response.Status.BAD_REQUEST)
						.tag("Object Fetch Error : " + ex.toString()).build();
			}
		}
	}

	/**
	 * <p>
	 * [9.2] Create a Container (CDMI Content Type) and [9.6] Update a Container
	 * (CDMI Content Type)
	 * </p>
	 * 
	 * @param path
	 *            Path to the new or existing container
	 * @param noClobber
	 *            Value of the no-clobber header (or "false" if not present)
	 * @param mustExist
	 *            Value of the must-exist header (or "false" if not present)
	 */
	@PUT
	@Path("/{path:.+}/")
	@Consumes(MediaTypes.CONTAINER)
	@Produces(MediaTypes.CONTAINER)
	public Response putContainer(
			@PathParam("path") String path,
			@HeaderParam("X-CDMI-NoClobber") @DefaultValue("false") String noClobber,
			@HeaderParam("X-CDMI-MustExist") @DefaultValue("false") String mustExist,
			byte[] bytes) {

		LOG.trace("In PathResource.putContainer, path is: {}", path);

		if (LOG.isTraceEnabled()) {
			String inBuffer = new String(bytes);
			LOG.trace("Request = {}", inBuffer);
		}

		Container containerRequest = new Container();

		try {
			containerRequest.fromJson(bytes, false);
			Container container = containerDao.createByPath(path,
					containerRequest);
			if (container == null) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			} else {
				// make http response
				// build a JSON representation
				String respStr = container.toJson(false);
				ResponseBuilder builder = Response.created(new URI(path));
				builder.header("X-CDMI-Specification-Version", "1.0.2");
				// ResponseBuilder builder =
				// Response.status(Response.Status.CREATED);
				return builder.entity(respStr).build();
				/*
				 * return Response.created(respStr).header(
				 * "X-CDMI-Specification-Version", "1.0.2").build();
				 */
			} // if/else
		} catch (Exception ex) {
			LOG.error("Failed to find container", ex);
			return Response.status(Response.Status.BAD_REQUEST)
					.tag("Object Creation Error : " + ex.toString()).build();
		}
	}

	/**
	 * <p>
	 * [8.2] Create a Data Object (CDMI Content Type) [8.6] Update Data Object
	 * (CDMI Content Type)
	 * </p>
	 * 
	 * @param path
	 *            Path to the parent container for the new data object
	 * @param mediaType
	 *            Declared media type of the data object
	 * @param dataObject
	 *            Raw content of the new data object
	 */
	@PUT
	@Path("/{path:.+}")
	@Consumes(MediaTypes.DATA_OBJECT)
	@Produces(MediaTypes.DATA_OBJECT)
	public Response putDataObjectCDMI(@Context HttpHeaders headers,
			@PathParam("path") String path, byte[] bytes) {

		LOG.trace("putDataObject(): ");
		if (LOG.isTraceEnabled()) {
			// print headers for debug
			for (String hdr : headers.getRequestHeaders().keySet()) {
				LOG.trace("{} - {}", hdr, headers.getRequestHeader(hdr));
			}
			String inBuffer = new String(bytes);
			LOG.trace("Path = {} {}", path, inBuffer);
		}

		try {
			DataObject oldObj = dataObjectDao.findByPath(path);
			// new object
			if (oldObj == null) {
				oldObj = new DataObject();

				oldObj.setObjectType("application/cdmi-object");
				// parse json
				oldObj.fromJson(bytes, false);
				if (oldObj.getValue() == null) {
					oldObj.setValue("== N/A ==");
				}
				oldObj = dataObjectDao.createByPath(path, oldObj);
				// return representation
				String respStr = oldObj.toJson();
				return Response.created(URI.create(path))
						.header("X-CDMI-Specification-Version", "1.0.2")
						.type(MediaTypes.DATA_OBJECT).entity(respStr).build();
			}
			// update object
			DataObject newObj = new DataObject();
			newObj.fromJson(bytes, false);

			// access a key management system
			KMS kms = KMS.getInstance();

			if (newObj.getMimetype().equals(MediaTypes.JOSEJSON)) {

				// New mimetype is ciphertext
				if ("application/jose+json".equals(oldObj.getMimetype())) {

					// Existing mimetype is ciphertext
					// Ciphertext -> Ciphertext
					// first try to decrypt
					try {
						oldObj.decryptData(kms);
					} catch (BadRequestException ex) {
						// cannot decrypt, internal error
						LOG.error("Could not decrypt ciphertext", ex);
						return Response.status(
								Response.Status.INTERNAL_SERVER_ERROR).build();
					} catch (ForbiddenException ex) {
						// cannot decrypt, authorization error
						LOG.error("Could not decrypt ciphertext", ex);
						return Response.status(Response.Status.UNAUTHORIZED)
								.build();
					}

					// then reencrypt
					if (newObj.getMetadata().get("cdmi_enc_keyID") == null) {
						oldObj.setMetadata("cdmi_enc_keyID",
								oldObj.getObjectID());
					} else {
						oldObj.setMetadata("cdmi_enc_keyID", newObj
								.getMetadata().get("cdmi_enc_keyID"));
					}

					// Fetch key from the KMS, or create new when needed
					JsonWebKey key = kms.getKeyByID(oldObj.getMetadata().get(
							"cdmi_enc_keyID"));
					if (key == null) {
						key = kms.createKeyA256KW(oldObj.getMetadata().get(
								"cdmi_enc_keyID"));
					}
					try {
						oldObj.encryptData(MediaTypes.JOSEJSON, key);
					} catch (BadRequestException ex) {
						// cannot encrypt, internal error
						LOG.error("Could not encrypt to ciphertext", ex);
						return Response.status(
								Response.Status.INTERNAL_SERVER_ERROR).build();
					}

					// overwrite on filesystem
					dataObjectDao.deleteByPath(path);
					dataObjectDao.createByPath(path, oldObj);

					return Response.status(Response.Status.NO_CONTENT).build();

				} else {
					// Existing mimetype is plaintext
					// Plain -> Ciphertext

					if (newObj.getMetadata().get("cdmi_enc_keyID") == null) {
						oldObj.setMetadata("cdmi_enc_keyID",
								oldObj.getObjectID());
					} else {
						oldObj.setMetadata("cdmi_enc_keyID", newObj
								.getMetadata().get("cdmi_enc_keyID"));
					}

					// Fetch key from the KMS, or create new when needed
					JsonWebKey key = kms.getKeyByID(oldObj.getMetadata().get(
							"cdmi_enc_keyID"));
					if (key == null) {
						key = kms.createKeyA256KW(oldObj.getMetadata().get(
								"cdmi_enc_keyID"));
					}
					try {
						oldObj.encryptData(MediaTypes.JOSEJSON, key);
					} catch (BadRequestException ex) {
						// cannot encrypt, internal error
						LOG.error("Could not encrypt to ciphertext", ex);
						return Response.status(
								Response.Status.INTERNAL_SERVER_ERROR).build();
					}

					// overwrite on filesystem
					dataObjectDao.deleteByPath(path);
					dataObjectDao.createByPath(path, oldObj);

					return Response.status(Response.Status.NO_CONTENT).build();

				}
			} else // New mimetype is plaintext
			{
				if (oldObj.getMimetype().equals(MediaTypes.JOSEJSON)) {

					// Existing mimetype is ciphertext
					// Ciphertext -> Plain
					// Try to do the decryption of the object.
					try {
						oldObj.decryptData(kms);
					} catch (BadRequestException ex) {
						// cannot decrypt, internal error
						LOG.error("Could not decrypt ciphertext", ex);
						return Response.status(
								Response.Status.INTERNAL_SERVER_ERROR).build();
					} catch (ForbiddenException ex) {
						// cannot decrypt, authorization error
						LOG.info("Could not decrypt ciphertext", ex);
						return Response.status(Response.Status.UNAUTHORIZED)
								.build();
					}

					// TODO: Update additional properties.
					// overwrite on filesystem
					dataObjectDao.deleteByPath(path);
					dataObjectDao.createByPath(path, oldObj);

					return Response.status(Response.Status.NO_CONTENT).build();
				} else {
					// Existing mimetype is plaintext
					// Plain -> Plain
					oldObj.mergeDataObject(newObj);
					dataObjectDao.deleteByPath(path);
					dataObjectDao.createByPath(path, oldObj);
					return Response.status(Response.Status.NO_CONTENT).build();
				}
			}
		} catch (Exception ex) {
			LOG.error("Failed to find the data object", ex);
			return Response.status(Response.Status.BAD_REQUEST)
					.tag("Object PUT Error : " + ex.toString()).build();
		}
	}

	/**
	 * <p>
	 * [8.3] Create a new Data Object (Non-CDMI Content Type) and [8.7] Update a
	 * Data Object (Non-CDMI Content Type)
	 * </p>
	 * 
	 * @param path
	 *            Path to the new or existing data object
	 * @param mediaType
	 *            Declared media type of the data object
	 * @param dataObject
	 *            Raw content of the new data object
	 */
	@PUT
	@Path("/{path:.+}")
	public Response putDataObjectNonCDMI(@PathParam("path") String path,
			@HeaderParam("Content-Type") String contentType, byte[] bytes) {
		LOG.trace("Non-CDMI putDataObject(): type={}, size={}, path={}",
				contentType, bytes.length, path);

		try {
			DataObject dObj = dataObjectDao.findByPath(path);
			if (dObj == null) {
				dObj = new DataObject();

				dObj.setObjectType("application/cdmi-object");
				// parse json
				// dObj.fromJson(bytes, false);
				if (dObj.getValue() == null) {
					dObj.setValue(bytes);
				}
				if ("application/octet-stream".equals(contentType)
						|| "image/gif".equals(contentType)
						|| "image/jpeg".equals(contentType)
						|| "image/png".equals(contentType)) {
					dObj.setValueTransferEncoding("base64");
					dObj.setValue(Base64.encodeBase64(bytes));
				} else {
					dObj.setValueTransferEncoding("utf-8");
					dObj.setValue(bytes);
				}
				LOG.trace("Calling createNonCDMIByPath");
				dObj = dataObjectDao.createNonCDMIByPath(path, contentType,
						dObj);
				// return representation
				// String respStr = OldObj.toJson();
				// return Response.ok(respStr).header(
				// "X-CDMI-Specification-Version", "1.0.2").build();
			}
			// dObj.fromJson(bytes,false);
			return Response.created(URI.create(path)).build();
		} catch (Exception ex) {
			LOG.error("Failed to find data object", ex);
			return Response.status(Response.Status.BAD_REQUEST)
					.tag("Object PUT Error : " + ex.toString()).build();
		}
		// throw new UnsupportedOperationException(
		// "PathResource.putDataObject(Non-CDMI Content Type");
	}

	/**
	 * <p>
	 * [9.10] Create a New Data Object (NON-CDMI Content Type)
	 * </p>
	 * 
	 * @param path
	 *            Path to the new or existing data object
	 * @param object
	 *            value
	 */
	@Path("/{path:.+}")
	@POST
	public Response postDataObject(@PathParam("path") String path, byte[] bytes) {

		String inBuffer = new String(bytes);
		LOG.trace("Path = {} {}", path, inBuffer);

		boolean containerRequest = false;
		if (containerDao.isContainer(path)) {
			containerRequest = true;
		}

		try {
			String objectId = ObjectID.getObjectID(11);
			String objectPath = path + "/" + objectId;

			DataObject dObj = new DataObject();
			dObj.setObjectID(objectId);
			dObj.setObjectType(objectPath);
			dObj.setValue(inBuffer);

			LOG.trace("objectId = {}, objecctPath = {}", objectId, objectPath);

			dObj = dataObjectDao.createByPath(objectPath, dObj);

			if (containerRequest) {
				return Response.created(URI.create(path))
						.header("Location", dObj.getObjectType()).build();
			}
			return Response.created(URI.create(path)).build();
		} catch (Exception ex) {
			LOG.error("Failed to create data object", ex);
			return Response.status(Response.Status.BAD_REQUEST)
					.tag("Object Creation Error : " + ex.toString()).build();
		}
	}

	/**
	 * <p>
	 * [9.3] Create a Container (Non-CDMI Content Type)
	 * </p>
	 * 
	 * <p>
	 * FIXME - I do not see how to disambiguate this kind of call from creating
	 * a data object with a non-CDMI Content Type)
	 */
	// test the connection between cdmi_server and dac server
	// @GET
	// @Path("/{path:.+}")
	// @Consumes("application/test")
	// public Response getTest() throws Exception {
	// DefaultHttpClient httpclient = new DefaultHttpClient();
	// URI uri=new URI("http://192.168.199.222:8080/DAC/Container/");
	// Http
	// System.out.println("...............");
	// HttpGet req = new
	// HttpGet("http://192.168.199.222:8080/DAC/rest/DACContainer/Test1.txt");
	// HttpResponse response = httpclient.execute(req);
	// System.out.println(response.getEntity());
	// return Response.ok(response.getEntity()).build();
	// if (response.getStatusLine().getStatusCode() == 200) {
	// System.out.println("response 200!");
	// } else {
	// System.out.println("no response!");
	// }
	// HttpEntity httpEntity = response.getEntity();
	// InputStream instreams = httpEntity.getContent();
	// InputStreamReader r = new InputStreamReader(instreams);
	// BufferedReader buffer = new BufferedReader(r);
	//
	// StringBuffer s = new StringBuffer();
	// String line = "";
	// while ((line = buffer.readLine()) != null) {
	// s.append(line);
	// }
	// // System.out.println(s);
	// return Response.ok().entity(s.toString()).build();
	// }
	// @PUT
	// @Path("/{path:.+}/")
	// @Consumes("application/test")
	// public Response putTest(@PathParam("path") String path,
	// @Context HttpHeaders headers, byte[] bytes)
	// throws URISyntaxException, IOException {
	// HttpClient client=new DefaultHttpClient();
	// HttpGet req=new
	// HttpGet("http://192.168.199.222:8080/DAC/rest/DACContainer/Test2.txt");
	// req.addHeader("auth","Wang");
	// req.addHeader("content-type","application/dac-object");
	//
	// HttpResponse response=client.execute(req);
	// Header header=response.getFirstHeader("access-status");
	// String status=header.getValue();
	// LOG.info(status);
	// HttpEntity httpEntity = response.getEntity();
	// InputStream instreams = httpEntity.getContent();
	// InputStreamReader r = new InputStreamReader(instreams);
	// BufferedReader buffer = new BufferedReader(r);
	// StringBuffer s = new StringBuffer();
	// String line = "";
	// LOG.info("you are stupid!");
	// while ((line = buffer.readLine()) != null) {
	// s.append(line);
	// }
	// // System.out.println(s);
	// return Response.ok().entity(s.toString()).build();
	// DacRequest dac = new DacRequest();
	// DacRequestEntity entity = new DacRequestEntity();
	// HashMap clientIdentity = new HashMap();
	// clientIdentity.put("acl_name", "jdoe");
	// clientIdentity.put("acl_group", "users");
	// entity.withCdmiObjectId("cdeuhr834384h8")
	// .withCdmiOperation(CdmiOperation.Opertion.cdmiRead)
	// .setClientIdentity(clientIdentity);
	// dac.operation(Request.Method.POST, entity.getJSONDacReqEntity());
	// return null;
	// }

}
