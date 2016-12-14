package org.snia.cdmiserver.dacHttp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.snia.cdmiserver.dacHttp.DacRequestEntity;

public class SecurityDacRequestEntity {

	private static JsonWebKey encDacPublicKey;
	private static RsaJsonWebKey sigCdmiPrivateKey = null;

	public static JsonWebKey getSigCdmiPrivateKey() {
		return sigCdmiPrivateKey;
	}

	public static JsonWebKey getEncDacPublicKey() {
		return encDacPublicKey;
	}

	static {
		StringBuilder jsonStr = new StringBuilder();
		try {

			String str = DacRequestEntity.class.getResource("/").toString();
			str = str.substring(6, str.length() - 8);

			FileInputStream fin = new FileInputStream(str
					+ "dac_encrypt_public.jwk.json");
			InputStreamReader in = new InputStreamReader(fin);
			BufferedReader reader = new BufferedReader(in);
			String s = "";
			while ((s = reader.readLine()) != null) {
				jsonStr.append(s);
			}
			encDacPublicKey = JsonWebKey.Factory.newJwk(jsonStr.toString());
		} catch (IOException | JoseException ex) {
			Logger.getLogger(DacRequestEntity.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	static {
		StringBuilder jsonStr = new StringBuilder();
		try {

			String str = DacRequestEntity.class.getResource("/").toString();
			str = str.substring(6, str.length() - 8);

			FileInputStream fin = new FileInputStream(str
					+ "cdmi_sign_private.jwk.json");
			InputStreamReader in = new InputStreamReader(fin);
			BufferedReader reader = new BufferedReader(in);
			String s = "";
			while ((s = reader.readLine()) != null) {
				jsonStr.append(s);
			}
			sigCdmiPrivateKey = (RsaJsonWebKey) JsonWebKey.Factory.newJwk(jsonStr.toString());
		} catch (IOException | JoseException ex) {
			Logger.getLogger(DacRequestEntity.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	public static String sigDacRequestEntity(String entity)
			throws JoseException {

		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(entity);
		String alg;
		if(sigCdmiPrivateKey.getAlgorithm()!=null){
			alg=sigCdmiPrivateKey.getAlgorithm();
		}else{
			alg=AlgorithmIdentifiers.RSA_USING_SHA256;
		}
		jws.setAlgorithmHeaderValue(alg);		
		jws.setKey(sigCdmiPrivateKey.getPrivateKey());
		
		if (sigCdmiPrivateKey.getKeyId() != null) {
			jws.setKeyIdHeaderValue(sigCdmiPrivateKey.getKeyId());
		}
		
		return jws.getFlattenedJsonSerialization();

	}

	public static String encryptDacRequestEntity(String entity)
			throws JoseException {
		JsonWebEncryption jwe = new JsonWebEncryption();
		jwe.setAlgorithmHeaderValue("RSA-OAEP-256");
		jwe.setPlaintext(entity);
		jwe.setKey(encDacPublicKey.getKey());
		jwe.setKeyIdHeaderValue(encDacPublicKey.getKeyId());
		jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");

		return jwe.getCompactSerialization();

	}

	public static void main(String[] args) {
		String entity = "{dac_request_version: 1,dac_request_id: 67a40b86-1794-4eab-8481-134c413d6ac0}";
		try {
			System.out.println(SecurityDacRequestEntity
					.encryptDacRequestEntity(entity));
		} catch (JoseException e) {
			e.printStackTrace();
		}
	}

}
