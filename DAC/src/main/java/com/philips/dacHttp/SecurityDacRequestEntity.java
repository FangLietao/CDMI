package com.philips.dacHttp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.apache.commons.logging.Log;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

public class SecurityDacRequestEntity {

	private static RsaJsonWebKey encDacPrivateKey;
	private static JsonWebKey sigCdmiPublicKey;

	private JsonWebSignature dac_request;
	private JsonWebKey dac_request_dest_certificate;
	private static String dac_request_dest_uri;

	public static String getDac_request_dest_uri() {
		return dac_request_dest_uri;
	}

	public JsonWebSignature getDac_request() {
		return dac_request;
	}

	public static JsonWebKey getSigCdmiPublicKey() {
		return sigCdmiPublicKey;
	}

	public static JsonWebKey getEncDacPrivateKey() {
		return encDacPrivateKey;
	}

	static {
		StringBuilder jsonStr = new StringBuilder();
		try {

			String str = DacRequestEntity.class.getResource("/").toString();
			str = str.substring(6, str.length() - 8);

			FileInputStream fin = new FileInputStream(str
					+ "dac_encrypt_private.jwk.json");
			InputStreamReader in = new InputStreamReader(fin);
			BufferedReader reader = new BufferedReader(in);
			String s = "";
			while ((s = reader.readLine()) != null) {
				jsonStr.append(s);
			}
			encDacPrivateKey = (RsaJsonWebKey) JsonWebKey.Factory.newJwk(jsonStr.toString());
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
					+ "cdmi_sign_public.jwk.json");
			InputStreamReader in = new InputStreamReader(fin);
			BufferedReader reader = new BufferedReader(in);
			String s = "";
			while ((s = reader.readLine()) != null) {
				jsonStr.append(s);
			}
			sigCdmiPublicKey = JsonWebKey.Factory.newJwk(jsonStr.toString());
		} catch (IOException | JoseException ex) {
			Logger.getLogger(DacRequestEntity.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	public SecurityDacRequestEntity(String JSONString) {
		JSONParser jp = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
		try {
			JSONObject jobj = (JSONObject) jp.parse(JSONString);

			if (jobj.containsKey("dac_request")) {
				JSONParser j = new JSONParser(
						JSONParser.DEFAULT_PERMISSIVE_MODE);
				String dacRequest = jobj.getAsString("dac_request");
				JSONObject jo = (JSONObject) j.parse(dacRequest);
				String compactSerialization = jo.getAsString("protected") + "."
						+ jo.getAsString("payload") + "." + jo.getAsString("signature");
				JsonWebSignature jws = new JsonWebSignature();
				jws.setCompactSerialization(compactSerialization);
				this.dac_request = jws;
			}

			if (jobj.containsKey("dac_request_dest_certificate")) {
				String jsonDest = jobj
						.getAsString("dac_request_dest_certificate");
				this.dac_request_dest_certificate = JsonWebKey.Factory
						.newJwk(jsonDest);
			}

			if (jobj.containsKey("dac_request_dest_uri")) {
				this.dac_request_dest_uri = jobj
						.getAsString("dac_request_dest_uri");
			}

		} catch (ParseException | JoseException e) {
			e.printStackTrace();
		}

	}

	public String sigVertifyDacRequestEntity(JsonWebSignature jws) {
		
		String playLoad = null;
		try {
			
			jws.setKey(sigCdmiPublicKey.getKey());
			// Signature Vertification
			if (!jws.verifySignature()) {
				throw new JoseException("Vertification failed");
			} else {

				playLoad = jws.getPayload();
			}

		} catch (JoseException e) {			
			e.printStackTrace();
		}
		return playLoad;

	}

	public String decryptDacResponseEntity(String CipherEntity)
			throws JoseException {
		JsonWebEncryption jwe = new JsonWebEncryption();

		jwe.setCompactSerialization(CipherEntity);
		jwe.setKey(encDacPrivateKey.getPrivateKey());

		return jwe.getPlaintextString();
	}

}
