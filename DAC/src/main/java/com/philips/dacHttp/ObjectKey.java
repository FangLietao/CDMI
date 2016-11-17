package com.philips.dacHttp;

import java.security.Key;

import net.minidev.json.JSONObject;

import org.jose4j.json.internal.json_simple.JSONArray;
import org.jose4j.jwk.JsonWebKey;

import com.philips.fakekms.KMS;
import com.philips.fakekms.KMSKeyExistsException;

public class ObjectKey {
//	private String kty;
//	private String alg;
//	private Key k;

	public static JsonWebKey getObjectKey(String kid) {
		if(kid==null){
			return null;
		}
		KMS kms = KMS.getInstance();
		JsonWebKey jwk = kms.getKeyByID(kid);
		if (jwk == null) {
			try {
				kms.createKeyA256KW(kid);
				jwk=kms.getKeyByID(kid);
			} catch (KMSKeyExistsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return jwk;
	}

//	public String toJSONString() {
//		JSONObject jObj = new JSONObject();
//		if (this.kty != null) {
//			jObj.put("kty", this.kty);
//		}
//		if (this.alg != null) {
//			jObj.put("alg", this.alg);
//		}		
//		if (this.k != null) {			
//	//		jObj.put("k", JSONArray.toJSONString(k.getEncoded()));	
//			jObj.put("k", new String(k.getEncoded()));
//		}
//
//		return jObj.toJSONString();
//	}

}
