package com.philips.resource;

import java.util.UUID;


import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.jose4j.jwk.JsonWebKey;

import net.minidev.json.parser.ParseException;

import com.philips.dacHttp.DacRequestEntity;
import com.philips.dacHttp.DacResponseEntity;
import com.philips.dacHttp.ObjectKey;

public class ObjectEncryptKeyResource {

	@POST
	@Path("/key/{path:.+}")
	public Response getKey(byte[] bytes) {
		
		System.out.println("enter key");

		DacResponseEntity responseEntity = new DacResponseEntity();
		try {
			DacRequestEntity reqEntity = new DacRequestEntity(bytes);
			JsonWebKey objKey = ObjectKey.getObjectKey((reqEntity
					.getCdmiEncKeyId()));
			responseEntity.withDacObjectKey(objKey).withDacResponseId(
					UUID.randomUUID().toString());

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return Response.ok(responseEntity.getJSONresponseEntity()).build();

	}

}
