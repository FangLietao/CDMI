package com.philips.dacHttp;

import org.jose4j.jwk.JsonWebKey;

import com.philips.fakekms.KMS;
import com.philips.fakekms.KMSKeyExistsException;

public class ObjectKey {

	public static JsonWebKey getObjectKey(String kid) {
		if (kid == null) {
			return null;
		}
		KMS kms = KMS.getInstance();
		JsonWebKey jwk = kms.getKeyByID(kid);
		if (jwk == null) {
			try {
				
				kms.createKeyA128KW(kid);
//				kms.createKeyA256KW(kid);
				jwk = kms.getKeyByID(kid);
			} catch (KMSKeyExistsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return jwk;
	}
}
