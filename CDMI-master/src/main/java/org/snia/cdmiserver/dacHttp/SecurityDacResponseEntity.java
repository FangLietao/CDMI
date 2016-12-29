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
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.snia.cdmiserver.dacHttp.DacRequestEntity;

public class SecurityDacResponseEntity {

	private static RsaJsonWebKey encCdmiPrivateKey;
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
			encCdmiPrivateKey = (RsaJsonWebKey) JsonWebKey.Factory
					.newJwk(jsonStr.toString());
		} catch (IOException | JoseException ex) {
			Logger.getLogger(DacRequestEntity.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	static {
		StringBuilder jsonStr = new StringBuilder();
		FileInputStream fin;
		InputStreamReader in;
		BufferedReader reader;
		try {

			String str = DacRequestEntity.class.getResource("/").toString();
			str = str.substring(6, str.length() - 8);

			fin = new FileInputStream(str + "dac_sign_public.jwk.json");
			in = new InputStreamReader(fin);
			reader = new BufferedReader(in);
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

	public SecurityDacResponseEntity(String JSONString) {
		JSONParser jp = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
		try {
			JSONObject jobj = (JSONObject) jp.parse(JSONString);

			if (jobj.containsKey("dac_response")) {
				JSONParser j = new JSONParser(
						JSONParser.DEFAULT_PERMISSIVE_MODE);
				String dacRequest = jobj.getAsString("dac_response");
				JSONObject jo = (JSONObject) j.parse(dacRequest);
				String compactSerialization = jo.getAsString("protected") + "."
						+ jo.getAsString("payload") + "."
						+ jo.getAsString("signature");
				JsonWebSignature jws = new JsonWebSignature();
				jws.setCompactSerialization(compactSerialization);
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

	private String sigVertifyDacResponseEntity() {

		String playLoad = null;
		try {
			this.dac_response.setKey(sigDacPublicKey.getKey());
			playLoad = this.dac_response.getPayload();

		} catch (JoseException e) {
			e.printStackTrace();
		}
		return playLoad;

	}

	private String decryptDacResponseEntity(String entity) throws JoseException {
		JsonWebEncryption jwe = new JsonWebEncryption();

		jwe.setCompactSerialization(entity);
		jwe.setKey(encCdmiPrivateKey.getPrivateKey());
//		jwe.setAlgorithmHeaderValue("RSA-OAEP-256");
		return jwe.getPlaintextString();
	}

	public DacResponseEntity getDacResponseEntity() {
		DacResponseEntity dacResponseEntity = new DacResponseEntity();
		String jws = sigVertifyDacResponseEntity();
		String jwe = null;
		try {
			jwe = decryptDacResponseEntity(jws);
			dacResponseEntity.fromJSONString(jwe);
		} catch (ParseException | JoseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dacResponseEntity;

	}

}
