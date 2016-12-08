package org.snia.cdmiserver.security;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.snia.cdmiserver.dacHttp.DacRequestEntity;

public class SignatureDacRequestEntity {
	private static JsonWebKey sigPrivateKey=null;
	
	public static JsonWebKey getSigPrivateKey() {
		return sigPrivateKey;
	}

	static {
		StringBuilder jsonStr = new StringBuilder();
		try {

			String str = DacRequestEntity.class.getResource("/").toString();
			str = str.substring(6, str.length() - 8);

			FileInputStream fin = new FileInputStream(str
					+ "cdmi_encrypt_private_jwk.json");
			InputStreamReader in = new InputStreamReader(fin);
			BufferedReader reader = new BufferedReader(in);
			String s = "";
			while ((s = reader.readLine()) != null) {
				jsonStr.append(s);
			}
			sigPrivateKey = JsonWebKey.Factory.newJwk(jsonStr.toString());
		} catch (IOException | JoseException ex) {
			Logger.getLogger(DacRequestEntity.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}
	
	public static String sigDacRequestEntity(String entity) throws JoseException{
		
		
		JsonWebSignature jws=new JsonWebSignature();
		jws.setPayload(entity);
		jws.setAlgorithmHeaderValue(ContentEncryptionAlgorithmIdentifiers.AES_128_GCM);
		jws.setKey(sigPrivateKey.getKey());
		if(sigPrivateKey.getKeyId()!=null){
			jws.setKeyIdHeaderValue(sigPrivateKey.getKeyId());
		}		
		
		return jws.getCompactSerialization();
		
	}

}
