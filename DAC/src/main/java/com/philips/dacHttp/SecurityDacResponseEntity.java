package com.philips.dacHttp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minidev.json.JSONObject;

import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

public class SecurityDacResponseEntity {

	private static JsonWebKey encCdmiPublicKey;
	private static RsaJsonWebKey sigDacPrivateKey = null;

	public static JsonWebKey getSigDacPrivateKey() {
		return sigDacPrivateKey;
	}

	public static JsonWebKey getEncCdmiPublicKey() {
		return encCdmiPublicKey;
	}

	static {
		StringBuilder jsonStr = new StringBuilder();
		try {

			String str = DacRequestEntity.class.getResource("/").toString();
			str = str.substring(6, str.length() - 8);

			FileInputStream fin = new FileInputStream(str
					+ "cdmi_encrypt_public.jwk.json");
			InputStreamReader in = new InputStreamReader(fin);
			BufferedReader reader = new BufferedReader(in);
			String s = "";
			while ((s = reader.readLine()) != null) {
				jsonStr.append(s);
			}
			encCdmiPublicKey = JsonWebKey.Factory.newJwk(jsonStr.toString());
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
					+ "dac_sign_private.jwk.json");
			InputStreamReader in = new InputStreamReader(fin);
			BufferedReader reader = new BufferedReader(in);
			String s = "";
			while ((s = reader.readLine()) != null) {
				jsonStr.append(s);
			}
			sigDacPrivateKey = (RsaJsonWebKey) JsonWebKey.Factory
					.newJwk(jsonStr.toString());
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
		if (sigDacPrivateKey.getAlgorithm() != null) {
			alg = sigDacPrivateKey.getAlgorithm();
		} else {
			alg = AlgorithmIdentifiers.RSA_USING_SHA256;
		}
		jws.setAlgorithmHeaderValue(alg);
		jws.setKey(sigDacPrivateKey.getPrivateKey());

		if (sigDacPrivateKey.getKeyId() != null) {
			jws.setKeyIdHeaderValue(sigDacPrivateKey.getKeyId());
		}

		return jws.getFlattenedJsonSerialization();

	}

	public static String encryptDacRequestEntity(String entity)
			throws JoseException {
		JsonWebEncryption jwe = new JsonWebEncryption();
		jwe.setAlgorithmHeaderValue("RSA-OAEP-256");
		jwe.setPlaintext(entity);
		jwe.setKey(encCdmiPublicKey.getKey());
		jwe.setKeyIdHeaderValue(encCdmiPublicKey.getKeyId());
		jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");

		return jwe.getCompactSerialization();

	}

	public String getSecurityRequestEntity(DacResponseEntity entity) {
		JSONObject jobj = new JSONObject();

		try {
			String jwe = encryptDacRequestEntity(entity.getJSONResponseEntity());
			String jws = sigDacRequestEntity(jwe);

			jobj.put("dac_response", jws);
			jobj.put("dac_response_dest_certificate", getEncCdmiPublicKey()
					.toJson());
			jobj.put("dac_response_dest_uri",
					SecurityDacRequestEntity.getDac_request_dest_uri());

		} catch (JoseException e) {
			System.out.println("encrypt or signature response entity failed");
			e.printStackTrace();
		}
		return jobj.toJSONString();
	}

}
