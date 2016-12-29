/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.snia.cdmiserver.dacHttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.snia.cdmiserver.model.DataObject;
import org.snia.cdmiserver.util.ACLIdentifier;

/**
 * 
 * @author 310253695
 */
public class DacRequest {

	// String pathDac = "http://192.168.1.201:8080/DAC";
	// String pathKey = "http://192.168.1.201:8080/DAC/key/";

	// public enum method {
	//
	// }

	// public void setPathDac(String pathDac) {
	// this.pathDac = pathDac;
	// }

	public DacResponseEntity operation(Request.Method method,
			DacRequestEntity entity, String pathDac) throws Exception {
		return operation(method, entity.getJSONDacReqEntity(), pathDac);
	}

	public DacResponseEntity operation(Request.Method method, String entity,
			String pathDac) throws Exception, IOException {
		return operation(method, entity, pathDac, false);
	}

	public DacResponseEntity operation(Request.Method method,
			DacRequestEntity entity, String pathDac, boolean isGetKey)
			throws URISyntaxException, IOException {
		return operation(method, entity.getJSONDacReqEntity(), pathDac,
				isGetKey);
	}

	public DacResponseEntity operation(Request.Method method, String entity,
			String pathKey, boolean isGetKey) throws URISyntaxException,
			IOException {
		Request req;
		if (!isGetKey) {
			req = new Request(method, pathKey);
		} else {
			req = new Request(method, pathKey + "/key/");
		}

		HttpResponse response = req.withContentType("application/dac-object")
				.withEntity(entity).send();

		HttpEntity httpEntity = response.getEntity();
		InputStream instreams = httpEntity.getContent();
		InputStreamReader r = new InputStreamReader(instreams);
		BufferedReader buffer = new BufferedReader(r);

		StringBuilder s = new StringBuilder();
		String line;
		while ((line = buffer.readLine()) != null) {
			s.append(line);
		}

		SecurityDacResponseEntity securityDacResponseEntity = new SecurityDacResponseEntity(
				s.toString());

		DacResponseEntity resEntity = securityDacResponseEntity
				.getDacResponseEntity();

		return resEntity;

	}

	public DacRequestEntity getRequestEntity(String user, DataObject dObj,
			CdmiOperation.Opertion operation, String keyId) {
		return getRequestEntity(user, dObj, operation.toString(), keyId);
	}

	public DacRequestEntity getRequestEntity(String user, DataObject dObj,
			CdmiOperation.Opertion operation) {
		return getRequestEntity(user, dObj, operation.toString(), null);
	}

	public DacRequestEntity getRequestEntity(String user, DataObject dObj,
			String operation, String keyId) {
		String identifier;
		// user = headers.getRequestHeader("user").get(0);
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

		DacRequestEntity reqEntity = new DacRequestEntity();
		reqEntity.withCdmiObjectId(dObj.getObjectID())
				.withDacRequestId(UUID.randomUUID().toString())
				.withAclEffectiveMask(operation)
				.withClientIdentity(clientIdentity).withCdmiEncKeyId(keyId);

		return reqEntity;
	}

	public String getSecurityRequestEntity(DacRequestEntity entity,
			String jwkString, String pathDac) {
		SecurityDacRequestEntity securityDacRequestEntity = new SecurityDacRequestEntity();
		return securityDacRequestEntity.getSecurityRequestEntity(entity,
				jwkString, pathDac);

	}

}
