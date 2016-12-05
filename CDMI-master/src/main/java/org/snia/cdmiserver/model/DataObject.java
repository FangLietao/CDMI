/*
 * Copyright (c) 2010, Sun Microsystems, Inc.
 * Copyright (c) 2010, The Storage Networking Industry Association.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of The Storage Networking Industry Association (SNIA) nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 *  THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.snia.cdmiserver.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.exception.BadRequestException;
import org.snia.cdmiserver.exception.ForbiddenException;
import org.snia.cdmiserver.resource.PathResource;
import org.snia.cdmiserver.util.JSONCompacter;
import org.snia.cdmiserver.util.MediaTypes;
import org.snia.fakekms.KMS;

/**
 * <p>
 * Representation of a CDMI <em>DataObject</em>.
 * </p>
 */
public class DataObject {

	private static final Logger LOG = LoggerFactory.getLogger(DataObject.class);

	// DataObject creation fields
	private String mimetype;
	private Map<String, String> metadata = new HashMap<String, String>();
	private String deserialize;
	private String serialize;
	private String copy;
	private String move;
	private String reference;
	private String value;
	private byte[] binaryValue;
	// DataObject representation fields
	private String objectType;
	private String objectID;
	private String objectName;
	private String parentURI;
	private String accountURI;
	private String capabilitiesURI;
	private String completionStatus;
	private Integer percentComplete; // FIXME - Specification says String but
										// that does not make
	// sense
	private String valuerange;
	private String valueTransferEncoding;

	// Representation also includes "mimetype", "metadata", and "value" from
	// creation fields
	//
	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(String key, String val) {
		metadata.put(key, val);
	}

	public String getDeserialize() {
		return deserialize;
	}

	public void setDeserialize(String deserialize) {
		this.deserialize = deserialize;
	}

	public String getSerialize() {
		return serialize;
	}

	public void setSerialize(String serialize) {
		this.serialize = serialize;
	}

	public String getCopy() {
		return copy;
	}

	public void setCopy(String copy) {
		this.copy = copy;
	}

	public String getMove() {
		return move;
	}

	public void setMove(String move) {
		this.move = move;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectURI) {
		this.objectType = objectURI;
	}

	public String getObjectID() {
		return objectID;
	}

	public void setObjectID(String objectID) {
		this.objectID = objectID;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getParentURI() {
		return parentURI;
	}

	public void setParentURI(String parentURI) {
		this.parentURI = parentURI;
	}

	public String getAccountURI() {
		return accountURI;
	}

	public void setAccountURI(String accountURI) {
		this.accountURI = accountURI;
	}

	public String getCapabilitiesURI() {
		return capabilitiesURI;
	}

	public void setCapabilitiesURI(String capabilitiesURI) {
		this.capabilitiesURI = capabilitiesURI;
	}

	public String getCompletionStatus() {
		return completionStatus;
	}

	public void setCompletionStatus(String completionStatus) {
		this.completionStatus = completionStatus;
	}

	public Integer getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(Integer percentComplete) {
		this.percentComplete = percentComplete;
	}

	public void setValueTransferEncoding(String valueTransferEncoding) {
		this.valueTransferEncoding = valueTransferEncoding;
	}

	public String getValueTransferEncoding() {
		return this.valueTransferEncoding;
	}

	public String getValuerange() {
		return valuerange;
	}

	public void setValuerange(String valuerange) {
		this.valuerange = valuerange;
	}

	public String toJson() throws Exception {
		//
		try {
			JSONObject j = new JSONObject();

			//
			if (objectType != null) {
				j.put("objectType", objectType);
			}
			if (objectName != null) {
				j.put("objectName", objectName);
			}
			if (capabilitiesURI != null) {
				j.put("capabilitiesURI", capabilitiesURI);
			}
			if (objectID != null) {
				j.put("objectID", objectID);
			}
			if (mimetype != null) {
				j.put("mimetype", mimetype);
			}
			//
			JSONObject m = new JSONObject();
			for (Map.Entry<String, String> entry : metadata.entrySet()) {
				m.put(entry.getKey(), entry.getValue());
			}
			j.put("metadata", m);
			//

			if (valueTransferEncoding != null) {
				j.put("valueTransferEncoding", valueTransferEncoding);
			}
			if (value != null) {
				j.put("valueRange", value.length() + "");
			}
			if (value != null) {
				j.put("value", value);
			}

			return j.toJSONString(JSONStyle.NO_COMPRESS);
			//
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
			// return ("Error : " + ex);
		}
		//

	}

	public String metadataToJson() throws Exception {
		//
		try {
			JSONObject j = new JSONObject();
			if (objectType != null) {
				j.put("objectType", objectType);
			}
			if (capabilitiesURI != null) {
				j.put("capabilitiesURI", capabilitiesURI);
			}
			if (objectID != null) {
				j.put("objectID", objectID);
			}
			if (objectName != null) {
				j.put("objectName", objectName);
			}
			if (mimetype != null) {
				j.put("mimetype", mimetype);
			}
			if (valueTransferEncoding != null) {
				j.put("valuetransferencoding", valueTransferEncoding);
			}

			//
			JSONObject m = new JSONObject();
			for (Map.Entry<String, String> entry : metadata.entrySet()) {
				m.put(entry.getKey(), entry.getValue());
			}
			j.put("metadata", m);

			return j.toJSONString(JSONStyle.NO_COMPRESS);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
			// return ("Error : " + ex);
		}
		//
	}

	public void fromJson(InputStream jsonIs, boolean fromFile)
			throws ParseException {
		
		JsonObject json = Json.createReader(jsonIs).readObject();
		fromJson(json, fromFile);
	}

	public void fromJson(byte[] jsonBytes, boolean fromFile)
			throws ParseException {
		ByteArrayInputStream is=new ByteArrayInputStream(jsonBytes);
		JsonObject json = Json.createReader(is).readObject();
		fromJson(json, fromFile);
	}

	private void fromJson(JsonObject json, boolean fromFile) {

		if (json.containsKey("metadata")) {
			JsonObject m = json.getJsonObject("metadata");

			for (String entry : m.keySet()) {
				JsonValue v = m.get(entry);
				if (v.getValueType() != null) {
					if (v.getValueType().equals(ValueType.STRING)) {
						JsonString s = (JsonString) v;
						this.getMetadata().put(entry, s.getString());
					}
					if (v.getValueType().equals(ValueType.OBJECT)) {
						JsonObject o = (JsonObject) v;
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						Json.createWriter(os).writeObject(o);
						this.getMetadata().put(entry, os.toString());
					}
				}

			}

		}
		if (json.containsKey("valuetransferencoding")) {
			String vte = json.getString("valuetransferencoding");
			this.setValueTransferEncoding(vte);
		} else {
			this.setValueTransferEncoding("utf-8");
		}

		if (json.containsKey("value")) {

			// handling JSON input
			// parser automatically processes JSON subobject as utf-8
			if ("json".equals(getValueTransferEncoding())) {
				setValueTransferEncoding("utf-8");
			}

			String v = json.getString("value");
			this.setValue(v);
		}

		if (json.containsKey("objectName")) {
			this.setObjectName(json.getString("objectName"));
		}

		if (json.containsKey("mimetype")) {
			this.setMimetype(json.getString("mimetype"));
		}
		if (fromFile) {
			if (json.containsKey("objectType")) {
				this.setObjectType(json.getString("objectType"));
			}
			if (json.containsKey("capabilitiesURI")) {
				this.setCapabilitiesURI(json.getString("capabilitiesURI"));
			}
			if (json.containsKey("objectID")) {
				this.setObjectID(json.getString("objectID"));
			}
			if (json.containsKey("valueRange")) {
				this.setValuerange(json.getString("valueRange"));
			}

		}

	}

	public void setValue(byte[] bytes) {
		this.value = new String(bytes);
	}

	public void decryptData(KMS kms) throws BadRequestException {
		switch (this.mimetype) {
		case "application/jose+json": {
			this.decryptDataFomJOSEJSON(kms);
			break;
		}
		}

	}

	public void decryptDataFomJOSEJSON(JsonWebKey jwk) {
		// parse ciphertext as JSON object
		String cipherText = this.getValue();
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject json;
		try {
			json = (JSONObject) parser.parse(cipherText);
		} catch (ParseException ex) {
			LOG.error("Could not parse ciphertext in Object Value");
			throw new BadRequestException(
					"Could not parse JSON in Object value", ex);
		}
		String compactciphertext = JSONCompacter.JSONToCompactJWE(json);

		// set up JWE object
		JsonWebEncryption jwe = new JsonWebEncryption();
		try {
			jwe.setCompactSerialization(compactciphertext);
		} catch (JoseException ex) {
			LOG.error("Could not parse JWE compact serialization");
			throw new BadRequestException(
					"Could not parse JWE compact serialization", ex);
		}

		// Figure out whether to use symmetric key or private key
		if ("oct".equals(jwk.getKeyType())) {
			jwe.setKey(jwk.getKey());
		} else if ("EC".equals(jwk.getKeyType())
				|| "RSA".equals(jwk.getKeyType())) {
			PrivateKey sk = ((PublicJsonWebKey) jwk).getPrivateKey();
			if (sk != null) {
				jwe.setKey(sk);
			} else // only public key available
			{
				throw new ForbiddenException(
						"No key available; could not decrypt.");
			}
		}

		byte[] plain;
		try {
			plain = jwe.getPlaintextBytes();
		} catch (JoseException ex) {
			throw new BadRequestException("Could not decrypt ciphertext.");
		}

		// properly decrypted, update object.
		this.setMimetype(jwe.getContentTypeHeaderValue()); // set the mime type
		this.getMetadata().remove("cdmi_enc_keyID");
		this.setValue(Base64.encodeBase64Chunked(plain));
		this.setValueTransferEncoding("base64");
		this.setMetadata("cdmi_size",
				Integer.toString(this.getValue().length()));

	}

	private void decryptDataFomJOSEJSON(KMS kms) throws BadRequestException,
			ForbiddenException {

		// parse ciphertext as JSON object
		String cipherText = this.getValue();
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject json;
		try {
			json = (JSONObject) parser.parse(cipherText);
		} catch (ParseException ex) {
			LOG.error("Could not parse ciphertext in Object Value");
			throw new BadRequestException(
					"Could not parse JSON in Object value", ex);
		}
		String compactciphertext = JSONCompacter.JSONToCompactJWE(json);

		// set up JWE object
		JsonWebEncryption jwe = new JsonWebEncryption();
		try {
			jwe.setCompactSerialization(compactciphertext);
		} catch (JoseException ex) {
			LOG.error("Could not parse JWE compact serialization");
			throw new BadRequestException(
					"Could not parse JWE compact serialization", ex);
		}

		// identify the key used for encryption
		JsonWebKey key = null;
		String keyid = this.getMetadata().get("cdmi_enc_keyID");
		if (keyid == null) {
			// See if the ciphertext has a keyid
			String jweKid = jwe.getKeyIdHeaderValue();
			if (jweKid != null && jweKid != "") {
				keyid = jweKid;
				key = kms.getKeyByID(keyid);
			}
			// else use object-id
			if (key == null) {
				key = kms.getKeyByID(this.getObjectID());
			}
		} else {
			key = kms.getKeyByID(keyid);
		}

		if (key == null) {
			throw new ForbiddenException("No key available; could not decrypt.");
		}

		// Figure out whether to use symmetric key or private key
		if ("oct".equals(key.getKeyType())) {
			jwe.setKey(key.getKey());
		} else if ("EC".equals(key.getKeyType())
				|| "RSA".equals(key.getKeyType())) {
			PrivateKey sk = ((PublicJsonWebKey) key).getPrivateKey();
			if (sk != null) {
				jwe.setKey(sk);
			} else // only public key available
			{
				throw new ForbiddenException(
						"No key available; could not decrypt.");
			}
		}

		byte[] plain;
		try {
			plain = jwe.getPlaintextBytes();
		} catch (JoseException ex) {
			throw new BadRequestException("Could not decrypt ciphertext.");
		}

		// properly decrypted, update object.
		this.setMimetype(jwe.getContentTypeHeaderValue()); // set the mime type
															// here to decrypted
															// format
		this.getMetadata().remove("cdmi_enc_keyID");
		this.setValue(Base64.encodeBase64Chunked(plain));
		this.setValueTransferEncoding("base64");
		this.setMetadata("cdmi_size",
				Integer.toString(this.getValue().length()));
	}

	public void encryptData(String targetmime, String enckeyid, KMS kms)
			throws BadRequestException {
		JsonWebKey encjwk = kms.getKeyByID(enckeyid);
		encryptData(targetmime, encjwk);

	}

	public void encryptData(String targetmime, JsonWebKey encjwk)
			throws BadRequestException {
		try {
			switch (targetmime) {
			case MediaTypes.JOSEJSON: {
				encryptDataToJOSEJSON(encjwk);
				break;
			}
			default:
				throw new BadRequestException("Encrypting to " + targetmime
						+ " is not supported");
			}
		} catch (JoseException ex) {
			throw new BadRequestException("Encrypting to " + targetmime
					+ " has failed", ex);
		}

	}

	private void encryptDataToJOSEJSON(JsonWebKey key) throws JoseException {

		// decode the plaintext from Base64 if necessary
		byte[] plain = null;
		if ("base64".equals(this.getValueTransferEncoding())) {
			plain = Base64.decodeBase64(this.getValue());
		} else if ("utf-8".equals(this.getValueTransferEncoding())) {
			try {
				plain = this.getValue().getBytes("UTF-8");
			} catch (UnsupportedEncodingException ex) {
				java.util.logging.Logger
						.getLogger(PathResource.class.getName()).log(
								Level.SEVERE, null, ex);
			}
		}

		JsonWebEncryption jwe = new JsonWebEncryption();
		jwe.setPlaintext(plain);
		jwe.setKey(key.getKey());
		jwe.setKeyIdHeaderValue(key.getKeyId());
		jwe.setAlgorithmHeaderValue(key.getAlgorithm());
		jwe.setContentTypeHeaderValue(this.getMimetype());
		jwe.setEncryptionMethodHeaderParameter(org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers.AES_128_GCM);
		String ciphertext = jwe.getCompactSerialization();

		JSONObject cip = JSONCompacter.CompactJWEToJSON(ciphertext);
		// cip.put("cty", doj.getMimetype());
		// cip.put("kid", key.getKeyId());
		this.setMetadata("cdmi_enc_keyID", key.getKeyId());
		this.setMimetype("application/jose+json");
		this.setValue(cip.toJSONString(JSONStyle.NO_COMPRESS));
		this.setValueTransferEncoding("utf-8");
		this.setMetadata("cdmi_size",
				Integer.toString(this.getValue().length()));
	}

	public void mergeDataObject(DataObject newDob) {
		if (newDob.getValue() != null) {
			this.setValue(newDob.getValue());
			this.setMetadata("cdmi_size",
					Integer.toString(newDob.getValue().length()));
		}
		if (newDob.getMimetype() != null) {
			this.setMimetype(newDob.getMimetype());
		}
		if (newDob.getMetadata() != null) {
			Map<String, String> metadata = newDob.getMetadata();
			Set<String> keySet = metadata.keySet();
			Iterator it = keySet.iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				if (key != null) {
					this.setMetadata(key, metadata.get(key));
				}
			}
		}
	}

}
