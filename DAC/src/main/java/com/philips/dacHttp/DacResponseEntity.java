package com.philips.dacHttp;

import java.util.Map;

import org.jose4j.jwk.JsonWebKey;

import net.minidev.json.JSONObject;

public class DacResponseEntity {
	private String dacResponseVersion="1";
	private String dacResponseId;
	private String dacIdentity;
	private String dacAppliedMask;
	private JsonWebKey dacObjectKey;
	private Map<String,String> dacResponseHeaders;
	private String dacRedirectObjectId;
	private String dacAuditUri;
	public DacResponseEntity withDacResponseVersion(String dacResponseVersion) {
		this.dacResponseVersion = dacResponseVersion;
		return this;
	}
	public DacResponseEntity withDacResponseId(String dacResponseId) {
		this.dacResponseId = dacResponseId;
		return this;
	}
	public DacResponseEntity withDacIdentity(String dacIdentity) {
		this.dacIdentity = dacIdentity;
		return this;
	}
	public DacResponseEntity withDacAppliedMask(String dacAppliedMask) {
		this.dacAppliedMask = dacAppliedMask;
		return this;
	}
	public DacResponseEntity withDacObjectKey(JsonWebKey dacObjectKey) {
		this.dacObjectKey = dacObjectKey;
		return this;
	}
	public DacResponseEntity withDacResponseHeaders(Map <String,String>dacResponseHeaders) {
		this.dacResponseHeaders = dacResponseHeaders;
		return this;
	}
	public DacResponseEntity withDacRedirectObjectId(String dacRedirectObjectId) {
		this.dacRedirectObjectId = dacRedirectObjectId;
		return this;
	}
	public DacResponseEntity withDacAuditUri(String dacAuditUri) {
		this.dacAuditUri = dacAuditUri;
		return this;
	}
	
	public String getJSONResponseEntity(){
		JSONObject jObj=new JSONObject();
		if(this.dacAppliedMask!=null){
			jObj.put("dacAppliedMask", dacAppliedMask);		
		}
		if(this.dacAuditUri!=null){
			jObj.put("dacAuditUri", dacAuditUri);
		}
		if(this.dacIdentity!=null){
			jObj.put("dacIdentity", dacIdentity);
		}
		if(this.dacRedirectObjectId!=null){
			jObj.put("dacRedirectObjectId", dacRedirectObjectId);
		}
		if(this.dacResponseId!=null){
			jObj.put("dacResponseId", dacResponseId);
		}
		if(this.dacResponseVersion!=null){
			jObj.put("dacResponseVersion", dacResponseVersion);
		}
		
		if(this.dacObjectKey!=null){			
			jObj.put("dacObjectKey", dacObjectKey.toJson());
		}
		
		if(this.dacResponseHeaders!=null){
			JSONObject resHeaders=new JSONObject();
			resHeaders.putAll(dacResponseHeaders);
			jObj.put("dacResponseHeaders", resHeaders);
		}
		
		return jObj.toJSONString();
	}
	
	

}
