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

import net.minidev.json.parser.ParseException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.snia.cdmiserver.model.DataObject;
import org.snia.cdmiserver.util.ACLIdentifier;

/**
 *
 * @author 310253695
 */
public class DacRequest {

    String path = "http://192.168.199.161:8080/DAC/rest/DACContainer/Test2.txt";

    public enum method {

    }

    public void setPath(String path) {
        this.path = path;
    }
    
    
    public DacResponseEntity operation(Request.Method method, DacRequestEntity entity) throws URISyntaxException, IOException{
    	return operation(method,entity.getJSONDacReqEntity());
    }

    public DacResponseEntity operation(Request.Method method, String entity) throws URISyntaxException, IOException {
        Request req = new Request(method, path);

        HttpResponse response = req.withContentType("application/dac-object").withEntity(entity).send();

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
            Logger.getLogger(DacRequest.class.getName()).log(Level.SEVERE, null, ex);
        }

        return resEntity;

    }
    
    public DacRequestEntity getRequestEntity(String user,DataObject dObj,CdmiOperation.Opertion operation,String keyId){
    	return getRequestEntity(user,dObj,operation.toString(),keyId);
    }
    
    public DacRequestEntity getRequestEntity(String user,DataObject dObj,CdmiOperation.Opertion operation){
    	return getRequestEntity(user,dObj,operation.toString(),null);
    }
    
    public DacRequestEntity getRequestEntity(String user,DataObject dObj,String operation,String keyId){
    	String identifier;
//		user = headers.getRequestHeader("user").get(0);
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
		reqEntity
				.withCdmiObjectId(dObj.getObjectID())
				.withDacRequestId(UUID.randomUUID().toString())
				.withAclEffectiveMask(operation)
				.withClientIdentity(clientIdentity).withCdmiEncKeyId(keyId);
    	
		return reqEntity;    	
    }
    
    
    

}
