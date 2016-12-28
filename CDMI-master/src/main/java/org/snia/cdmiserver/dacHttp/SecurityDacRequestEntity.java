package org.snia.cdmiserver.dacHttp;

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
import org.snia.cdmiserver.dacHttp.DacRequestEntity;

public class SecurityDacRequestEntity {
//	private static String pathDac = "http://192.168.17.207:8080/DAC";

	// private static JsonWebKey encDacPublicKey;
	private static RsaJsonWebKey sigCdmiPrivateKey = null;

	public static JsonWebKey getSigCdmiPrivateKey() {
		return sigCdmiPrivateKey;
	}

	// public static JsonWebKey getEncDacPublicKey() {
	// return encDacPublicKey;
	// }

	// static {
	// StringBuilder jsonStr = new StringBuilder();
	// try {
	//
	// String str = DacRequestEntity.class.getResource("/").toString();
	// str = str.substring(6, str.length() - 8);
	//
	// FileInputStream fin = new FileInputStream(str
	// + "dac_encrypt_public.jwk.json");
	// InputStreamReader in = new InputStreamReader(fin);
	// BufferedReader reader = new BufferedReader(in);
	// String s = "";
	// while ((s = reader.readLine()) != null) {
	// jsonStr.append(s);
	// }
	// encDacPublicKey = JsonWebKey.Factory.newJwk(jsonStr.toString());
	// } catch (IOException | JoseException ex) {
	// Logger.getLogger(DacRequestEntity.class.getName()).log(
	// Level.SEVERE, null, ex);
	// }
	// }

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
			sigCdmiPrivateKey = (RsaJsonWebKey) JsonWebKey.Factory
					.newJwk(jsonStr.toString());
		} catch (IOException | JoseException ex) {
			Logger.getLogger(DacRequestEntity.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	private String sigDacRequestEntity(String entity) throws JoseException {

		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(entity);
		String alg;
		if (sigCdmiPrivateKey.getAlgorithm() != null) {
			alg = sigCdmiPrivateKey.getAlgorithm();
		} else {
			alg = AlgorithmIdentifiers.RSA_USING_SHA256;
		}
		jws.setAlgorithmHeaderValue(alg);
		jws.setKey(sigCdmiPrivateKey.getPrivateKey());

		if (sigCdmiPrivateKey.getKeyId() != null) {
			jws.setKeyIdHeaderValue(sigCdmiPrivateKey.getKeyId());
		}

		return jws.getFlattenedJsonSerialization();

	}

	private String encryptDacRequestEntity(String entity, String jwkString)
			throws JoseException {
		JsonWebKey encDacPublicKey = JsonWebKey.Factory.newJwk(jwkString);
		JsonWebEncryption jwe = new JsonWebEncryption();
		jwe.setAlgorithmHeaderValue("RSA-OAEP-256");
		jwe.setPlaintext(entity);
		jwe.setKey(encDacPublicKey.getKey());
		jwe.setKeyIdHeaderValue(encDacPublicKey.getKeyId());
		jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");

		return jwe.getCompactSerialization();

	}

	public String getSecurityRequestEntity(DacRequestEntity entity,
			String jwkString,String pathDac) {
		JSONObject jobj = new JSONObject();

		try {
			String jwe = encryptDacRequestEntity(entity.getJSONDacReqEntity(),
					jwkString);
			String jws = sigDacRequestEntity(jwe);
			// String
			// project=Base64.toBase64String(EncryptDacRequestEntity.getEncPublicKey().toJson().getBytes());

			jobj.put("dac_request", jws);
			jobj.put("dac_request_dest_certificate", jwkString);
			jobj.put("dac_request_dest_uri", pathDac);

		} catch (JoseException e) {
			System.out.println("encrypt request entity failed");
			e.printStackTrace();
		}
		return jobj.toJSONString();
	}

}
