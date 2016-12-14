package org.snia.cdmiserver.dacHttp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.snia.cdmiserver.dacHttp.DacRequestEntity;

public class SecurityDacResponseEntity {

	private static JsonWebKey encCdmiPrivateKey;
	private static JsonWebKey sigDacPublicKey;

	private JsonWebSignature dac_response;
	private JsonWebKey dac_response_dest_certificate;
	private String dac_response_dest_uri;

	public static JsonWebKey getEncCdmiPrivateKey() {
		return encCdmiPrivateKey;
	}

	public static JsonWebKey getSigDacPublicKey() {
		return sigDacPublicKey;
	}

	static {
		StringBuilder jsonStr = new StringBuilder();
		try {

			String str = DacRequestEntity.class.getResource("/").toString();
			str = str.substring(6, str.length() - 8);

			FileInputStream fin = new FileInputStream(str
					+ "cdmi_encrypt_private.jwk.json");
			InputStreamReader in = new InputStreamReader(fin);
			BufferedReader reader = new BufferedReader(in);
			String s = "";
			while ((s = reader.readLine()) != null) {
				jsonStr.append(s);
			}
			encCdmiPrivateKey = JsonWebKey.Factory.newJwk(jsonStr.toString());
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
					+ "dac_encrypt_public.jwk.json");
			InputStreamReader in = new InputStreamReader(fin);
			BufferedReader reader = new BufferedReader(in);
			String s = "";
			while ((s = reader.readLine()) != null) {
				jsonStr.append(s);
			}
			sigDacPublicKey = JsonWebKey.Factory.newJwk(jsonStr.toString());
		} catch (IOException | JoseException ex) {
			Logger.getLogger(DacRequestEntity.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	public void getFromJSONString(String JSONString) {
		JSONParser jp = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
		try {
			JSONObject jobj = (JSONObject) jp.parse(JSONString);
			
			if (jobj.containsKey("dac_response")) {
				// JSONParser j = new JSONParser(
				// JSONParser.DEFAULT_PERMISSIVE_MODE);
				String dacResponse = jobj.getAsString("dac_response");
				JsonWebSignature jws = new JsonWebSignature();
				jws.setCompactSerialization(dacResponse);
				this.dac_response = jws;
			}

			if (jobj.containsKey("dac_response_dest_certificate")) {
				String jsonDest = jobj
						.getAsString("dac_response_dest_certificate");
				this.dac_response_dest_certificate = JsonWebKey.Factory
						.newJwk(jsonDest);
			}

			if (jobj.containsKey("dac_response_dest_uri")) {
				this.dac_response_dest_uri = jobj
						.getAsString("dac_response_dest_uri");
			}

		} catch (ParseException | JoseException e) {
			e.printStackTrace();
		}

	}

	public static String desigDacResponseEntity(JsonWebSignature jws,JsonWebKey jwk) {
		jws.setKey(jwk.getKey());
		String playLoad=null;
		try {
			playLoad=jws.getPayload();
			
		} catch (JoseException e) {			
			e.printStackTrace();
		}
		return playLoad;

	}

	public static String decryptDacResponseEntity(String entity)
			throws JoseException {
		JsonWebEncryption jwe = new JsonWebEncryption();

		jwe.setPayload(entity);
		jwe.setKey(encCdmiPrivateKey.getKey());

		return jwe.getPlaintextString();
	}

}
