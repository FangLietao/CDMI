package org.snia.cdmiserver.security;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;
import org.snia.cdmiserver.dacHttp.DacRequestEntity;

public class EncryptDacRequestEntity {
	private String keyPath="cdmi_encrypt_public_jwk.json";
	private static JsonWebKey encPublicKey;	
	
	public static JsonWebKey getEncPublicKey() {
		return encPublicKey;
	}	

	static {
		StringBuilder jsonStr = new StringBuilder();
		try {

			String str = DacRequestEntity.class.getResource("/").toString();
			str = str.substring(6, str.length() - 8);

			FileInputStream fin = new FileInputStream(str
					+ "cdmi_encrypt_public_jwk.json");
			InputStreamReader in = new InputStreamReader(fin);
			BufferedReader reader = new BufferedReader(in);
			String s = "";
			while ((s = reader.readLine()) != null) {
				jsonStr.append(s);
			}
			encPublicKey = JsonWebKey.Factory.newJwk(jsonStr.toString());
		} catch (IOException | JoseException ex) {
			Logger.getLogger(DacRequestEntity.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}
	
	
	public static String encryptDacRequestEntity(String entity) throws JoseException{
		JsonWebEncryption jwe=new JsonWebEncryption();
		jwe.setAlgorithmHeaderValue("RSA-OAEP");
		jwe.setPlaintext(entity);
		jwe.setKey(encPublicKey.getKey());
		jwe.setKeyIdHeaderValue(encPublicKey.getKeyId());
		jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_GCM);
		
		return jwe.getCompactSerialization();
		
	}
	 
	
	public static void main(String[] args){
		String entity="{dac_request_version: 1,dac_request_id: 67a40b86-1794-4eab-8481-134c413d6ac0}";
		try {
			System.out.println(EncryptDacRequestEntity.encryptDacRequestEntity(entity));
		} catch (JoseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
