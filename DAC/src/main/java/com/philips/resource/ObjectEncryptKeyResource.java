package com.philips.resource;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;

import net.minidev.json.parser.ParseException;

import com.philips.dacHttp.DacRequestEntity;
import com.philips.dacHttp.DacResponseEntity;
import com.philips.dacHttp.ObjectKey;
import com.philips.dacHttp.SecurityDacRequestEntity;
import com.philips.dacHttp.SecurityDacResponseEntity;
import com.philips.fakekms.KMS;
import com.philips.fakekms.KMSKeyErrorException;
import com.philips.fakekms.KMSKeyExistsException;

public class ObjectEncryptKeyResource {

	@POST
	@Path("/key/")
	public Response getKey(byte[] bytes) {

		DacResponseEntity responseEntity = new DacResponseEntity();

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

		JsonWebKey objKey = ObjectKey
				.getObjectKey((reqEntity.getCdmiEncKeyId()));
		responseEntity.withDacObjectKey(objKey).withDacResponseId(
				UUID.randomUUID().toString());
		
		SecurityDacResponseEntity securityDacResponseEntity = new SecurityDacResponseEntity();

		return Response.ok(
				securityDacResponseEntity.getSecurityRequestEntity(
						responseEntity, reqEntity.getServerIdentity())).build();

		// return Response.ok(responseEntity.getJSONResponseEntity()).build();

	}

	@PUT
	@Path("/upload")
	@Consumes("application/jwk+json")
	public Response uploadKey(@Context HttpHeaders headers, byte[] bytes) {

		// JsonWebKey jwk=JsonWebKey.Factory.newJwk(new String(bytes));
		KMS kms = KMS.getInstance();
		try {
			kms.addKeyFromString(new String(bytes));
		} catch (KMSKeyErrorException e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).tag("key format err")
					.build();
		} catch (KMSKeyExistsException e) {

			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).tag("key is exist")
					.build();
		}

		return Response.ok().build();

	}

}
