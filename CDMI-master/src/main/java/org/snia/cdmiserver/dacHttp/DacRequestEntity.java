/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.snia.cdmiserver.dacHttp;

import static com.sun.org.apache.bcel.internal.util.SecuritySupport.getResourceAsStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONObject;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;

/**
 * 
 * @author 310253695
 */
public class DacRequestEntity {

	private String dacRequestVersion = "1";
	private String dacRequestId;
	private static JsonWebKey serverIdentity; // information of CDMI server
												// public key
	private Map clientIdentity; //
	private String aclEffectiveMask;
	private Map clientHeaders;
	private String cdmiObjectId;
	private String cdmiEncKeyId; // optional cdmi server get the key by
									// requesting
	private CdmiOperation.Opertion cdmiOperation;
	private String dacResponseUri; // optional client de/encrypt

	public void setDacRequestVersion(String dacRequestVersion) {
		this.dacRequestVersion = dacRequestVersion;
	}

	public void setDacRequestId(String dacRequestId) {
		this.dacRequestId = dacRequestId;
	}

	public void setServerIdentity(JsonWebKey serverIdentity) {
		this.serverIdentity = serverIdentity;
	}

	public void setClientIdentity(Map clientIdentity) {
		this.clientIdentity = clientIdentity;
	}

	public void setAclEffectiveMask(String aclEffectiveMask) {
		this.aclEffectiveMask = aclEffectiveMask;
	}

	public void setClientHeaders(Map clientHeaders) {
		this.clientHeaders = clientHeaders;
	}

	public void setCdmiObjectId(String cdmiObjectId) {
		this.cdmiObjectId = cdmiObjectId;
	}

	public void setCdmiEncKeyId(String cdmiEncKeyId) {
		this.cdmiEncKeyId = cdmiEncKeyId;
	}

	public void setCdmiOperation(CdmiOperation.Opertion cdmiOperation) {
		this.cdmiOperation = cdmiOperation;
	}

	public void setDacResponseUri(String dacResponseUri) {
		this.dacResponseUri = dacResponseUri;
	}

	// initialize the attribe by with
	public DacRequestEntity withDacRequestVersion(String dacRequestVersion) {
		this.dacRequestVersion = dacRequestVersion;
		return this;
	}

	public DacRequestEntity withDacRequestId(String dacRequestId) {
		this.dacRequestId = dacRequestId;
		return this;
	}

	public DacRequestEntity withServerIdentity(JsonWebKey serverIdentity) {
		this.serverIdentity = serverIdentity;
		return this;
	}

	public DacRequestEntity withClientIdentity(Map clientIdentity) {
		this.clientIdentity = clientIdentity;
		return this;
	}

	public DacRequestEntity withAclEffectiveMask(String aclEffectiveMask) {
		this.aclEffectiveMask = aclEffectiveMask;
		return this;
	}

	public DacRequestEntity withClientHeaders(Map clientHeaders) {
		this.clientHeaders = clientHeaders;
		return this;
	}

	public DacRequestEntity withCdmiObjectId(String cdmiObjectId) {
		this.cdmiObjectId = cdmiObjectId;
		return this;
	}

	public DacRequestEntity withCdmiEncKeyId(String cdmiEncKeyId) {
		if(cdmiEncKeyId!=null){
			this.cdmiEncKeyId = cdmiEncKeyId;
		}		
		return this;
	}

	public DacRequestEntity withCdmiOperation(
			CdmiOperation.Opertion cdmiOperation) {
		this.cdmiOperation = cdmiOperation;
		return this;
	}

	public DacRequestEntity withDacResponseUri(String dacResponseUri) {
		this.dacResponseUri = dacResponseUri;
		return this;
	}

	static {
		//cdmi server public key
		StringBuilder jsonStr = new StringBuilder();
		try {

			String str = DacRequestEntity.class.getResource("/").toString();
			str = str.substring(6, str.length()-8);			
			
			FileInputStream fin = new FileInputStream(str+"cdmi_encrypt_public_jwk.json");
			InputStreamReader in = new InputStreamReader(fin);
			BufferedReader reader = new BufferedReader(in);
			String s = "";
			while ((s = reader.readLine()) != null) {
				jsonStr.append(s);
			}			
			JsonWebKey encPublicKey = JsonWebKey.Factory.newJwk(jsonStr
					.toString());
			serverIdentity = encPublicKey;
		} catch (IOException | JoseException ex) {
			Logger.getLogger(DacRequestEntity.class.getName()).log(
					Level.SEVERE, null, ex);
		}

	}

	public String getJSONDacReqEntity() {
		JSONObject jObj = new JSONObject();
		if (this.aclEffectiveMask != null) {
			jObj.put("aclEffectiveMask", aclEffectiveMask);
		}
		if (this.cdmiEncKeyId != null) {
			jObj.put("cdmiEncKeyId", cdmiEncKeyId);
		}
		if (this.cdmiObjectId != null) {
			jObj.put("cdmiObjectId", cdmiObjectId);
		}
		if (this.cdmiOperation != null) {
			jObj.put("cdmiOperation", cdmiOperation.toString());
		}
		if (this.dacRequestId != null) {
			jObj.put("dacRequestId", dacRequestId);
		}
		if (this.dacRequestVersion != null) {
			jObj.put("dacRequestVersion", dacRequestVersion);
		}
		if (this.dacResponseUri != null) {
			jObj.put("dacResponseUri", dacResponseUri);
		}
		if (serverIdentity != null) {
			jObj.put("serverIdentity", serverIdentity.toJson());

			// JSONObject jServerIdentity=new JSONObject();
			// jServerIdentity.putAll(serverIdentity);
			// jObj.put("serverIdentity", jServerIdentity);
		}

		if (this.clientHeaders != null) {
			JSONObject jClientHeaders = new JSONObject();
			jClientHeaders.putAll(clientHeaders);
			jObj.put("clientHeaders", jClientHeaders);
		}

		if (this.clientIdentity != null) {
			JSONObject jClientIdentity = new JSONObject();
			jClientIdentity.putAll(clientIdentity);
			jObj.put("clientIdentity", jClientIdentity);
		}

		return jObj.toJSONString();
	}

}
