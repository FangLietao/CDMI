package com.philips.dacHttp;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class DacRequestEntity {
	private String dacRequestVersion = "1";
	private String dacRequestId;
	private JsonWebKey serverIdentity;
	private Map<String, String> clientIdentity;
	private String aclEffectiveMask;
	private Map<String, String> clientHeaders;
	private String cdmiObjectId;
	private String cdmiEncKeyId; // optional cdmi server get the key by
									// requesting
	private String cdmiOperation;
	private String dacResponseUri; // optional client de/encrypt

	public String getDacRequestVersion() {
		return dacRequestVersion;
	}

	public String getDacRequestId() {
		return dacRequestId;
	}

	public JsonWebKey getServerIdentity() {
		return serverIdentity;
	}

	public Map<String, String> getClientIdentity() {
		return clientIdentity;
	}

	public String getAclEffectiveMask() {
		return aclEffectiveMask;
	}

	public Map<String, String> getClientHeaders() {
		return clientHeaders;
	}

	public String getCdmiObjectId() {
		return cdmiObjectId;
	}

	public String getCdmiEncKeyId() {
		return cdmiEncKeyId;
	}

	public String getCdmiOperation() {
		return cdmiOperation;
	}

	public String getDacResponseUri() {
		return dacResponseUri;
	}

	public DacRequestEntity(String JSONString) throws ParseException {
		fromJSONString(JSONString.getBytes());
	}

	public DacRequestEntity(byte[] JSONBytes) throws ParseException {
		fromJSONString(JSONBytes);
	}

	public DacRequestEntity fromJSONString(byte[] JSONBytes)
			throws ParseException {
		JSONParser jp = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject jObj = (JSONObject) jp.parse(JSONBytes);

		if (jObj.containsKey("aclEffectiveMask")) {
			this.aclEffectiveMask = jObj.getAsString("aclEffectiveMask");
		}
		if (jObj.containsKey("cdmiObjectId")) {
			this.cdmiObjectId = jObj.getAsString("cdmiObjectId");
		}
		if (jObj.containsKey("cdmiEncKeyId")) {
			String keyId = jObj.getAsString("cdmiEncKeyId");
			if (keyId.equals("objectId")) {
				this.cdmiEncKeyId = this.cdmiObjectId;
			} else {
				this.cdmiEncKeyId = jObj.getAsString("cdmiEncKeyId");
			}

		}

		if (jObj.containsKey("cdmiOperation")) {
			this.cdmiOperation = jObj.getAsString("cdmiOperation");
		}
		if (jObj.containsKey("dacRequestId")) {
			this.dacRequestId = jObj.getAsString("dacRequestId");
		}
		if (jObj.containsKey("dacRequestVersion")) {
			this.dacRequestVersion = jObj.getAsString("dacRequestVersion");
		}

		if (jObj.containsKey("dacResponseUri")) {
			this.dacResponseUri = jObj.getAsString("dacResponseUri");
		}

		if (jObj.containsKey("serverIdentity")) {
			try {
				this.serverIdentity = JsonWebKey.Factory.newJwk(jObj
						.getAsString("serverIdentity"));
			} catch (JoseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if (jObj.containsKey("clientHeaders")) {
			clientHeaders = new HashMap<String, String>();
			JSONObject jClientHeaders = (JSONObject) jObj.get("clientHeaders");
			for (Map.Entry<String, Object> entry : jClientHeaders.entrySet()) {
				this.clientHeaders.put(entry.getKey(),
						(String) entry.getValue());
			}
		}

		if (jObj.containsKey("clientIdentity")) {
			clientIdentity = new HashMap<String, String>();
			JSONObject jClientIdentity = (JSONObject) jObj
					.get("clientIdentity");
			for (Map.Entry<String, Object> entry : jClientIdentity.entrySet()) {
				this.clientIdentity.put(entry.getKey(),
						(String) entry.getValue());
			}
		}

		return this;
	}
}
