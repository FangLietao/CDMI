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
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.bouncycastle.util.encoders.Base64;
import org.jose4j.lang.JoseException;
import org.snia.cdmiserver.model.DataObject;
import org.snia.cdmiserver.util.ACLIdentifier;

/**
 * 
 * @author 310253695
 */
public class DacRequest {

	String pathDac = "http://192.168.17.207:8080/DAC";
	String pathKey = "http://192.168.17.207:8080/DAC/key/";

	public enum method {

	}

	public void setPathDac(String pathDac) {
		this.pathDac = pathDac;
	}

	public DacResponseEntity operation(Request.Method method,
			DacRequestEntity entity) throws Exception {
		return operation(method, entity.getJSONDacReqEntity());
	}

	public DacResponseEntity operation(Request.Method method, String entity)
			throws Exception, IOException {
		return operation(method, entity, false);
	}

	public DacResponseEntity operation(Request.Method method,
			DacRequestEntity entity, boolean isGetKey)
			throws URISyntaxException, IOException {
		return operation(method, entity.getJSONDacReqEntity(), isGetKey);
	}

	public DacResponseEntity operation(Request.Method method, String entity,
			boolean isGetKey) throws URISyntaxException, IOException {
		Request req;
		if (isGetKey) {
			req = new Request(method, pathKey);
		} else {
			req = new Request(method, pathDac);
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

		DacResponseEntity resEntity = new DacResponseEntity();
		try {
			resEntity.fromJSONString(s.toString());
		} catch (ParseException ex) {
			Logger.getLogger(DacRequest.class.getName()).log(Level.SEVERE,
					null, ex);
		}

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

	public String getSecurityRequestEntity(DacRequestEntity entity) {
		JSONObject jobj = new JSONObject();
		;
		try {
			String jwe = SecurityDacRequestEntity.encryptDacRequestEntity(entity
					.getJSONDacReqEntity());
			String jws = SecurityDacRequestEntity.sigDacRequestEntity(jwe);
			// String
			// project=Base64.toBase64String(EncryptDacRequestEntity.getEncPublicKey().toJson().getBytes());

			jobj.put("dac_request", jws);
			jobj.put("dac_request_dest_certificate",
					SecurityDacRequestEntity.getEncDacPublicKey().toJson());
			jobj.put("dac_request_dest_uri", this.pathDac);

		} catch (JoseException e) {
			System.out.println("encrypt request entity failed");
			e.printStackTrace();
		}
		return jobj.toJSONString();
	}

	public String parseSecurityResponseEntity(DacResponseEntity entity) {
		
		
		
		return null;
	}

}
