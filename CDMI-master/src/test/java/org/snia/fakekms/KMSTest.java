package org.snia.fakekms;

import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class KMSTest {

	// @Test
	// public void testKMS() throws KMSKeyExistsException, JoseException {
	//
	// // create instance of the KMS
	// KMS testkms = KMS.getInstance();
	// assertNotNull("KMS was instantiated", testkms);
	// assertNotNull("KMS has a valid keyset object", testkms.jwkSet);
	//
	// // empty the KMS
	// testkms.cleanKMS();
	// assertEquals("KMS has correctly cleaned the keylist",
	// 0,testkms.jwkSet.getJsonWebKeys().size());
	//
	// JsonWebKey jwk;
	// JsonWebEncryption jwe;
	// JsonWebSignature jws;
	// String plaintext = "This is some plaintext";
	// String ciphertext;
	//
	// // create AES encryption key
	// jwk = testkms.createKeyA256KW("unittest_key_1");
	// assertEquals("Key created with correct type", "oct", jwk.getKeyType());
	// assertEquals("Key created with correct use",org.jose4j.jwk.Use.ENCRYPTION,
	// jwk.getUse());
	// assertEquals("Key created with correct algorithm","A256KW",
	// jwk.getAlgorithm());
	//
	// // create an encryption
	// jwe = new JsonWebEncryption();
	// jwe.setPlaintext(plaintext);
	// jwe.setKey(jwk.getKey());
	// jwe.setKeyIdHeaderValue(jwk.getKeyId());
	// jwe.setAlgorithmHeaderValue(jwk.getAlgorithm());
	// jwe.setEncryptionMethodHeaderParameter("A256GCM");
	// ciphertext = jwe.getCompactSerialization();
	//
	// // perform decryption
	// jwe.setCompactSerialization(ciphertext);
	// jwe.setKey(testkms.getKeyByID(jwe.getKeyIdHeaderValue()).getKey());
	// assertEquals("AES decryption performed okay", plaintext,
	// jwe.getPlaintextString());
	//
	//
	// // create HMAC signing key
	// jwk = testkms.createKeyHS256("unittest_key_2");
	// assertEquals("Key created with correct type","oct", jwk.getKeyType());
	// assertEquals("Key created with correct use",org.jose4j.jwk.Use.SIGNATURE,
	// jwk.getUse());
	// assertEquals("Key created with correct algorithm","HS256",
	// jwk.getAlgorithm());
	//
	// // create a signature
	// jws = new JsonWebSignature();
	// jws.setKey(jwk.getKey());
	// jws.setKeyIdHeaderValue(jwk.getKeyId());
	// jws.setAlgorithmHeaderValue(jwk.getAlgorithm());
	// ciphertext = jws.getCompactSerialization();
	//
	// // verify a signature
	// jws = new JsonWebSignature();
	// jws.setCompactSerialization(ciphertext);
	// jws.setKey(testkms.getKeyByID(jws.getKeyIdHeaderValue()).getKey());
	// assertTrue("HMAC verification performed okay", jws.verifySignature());
	//
	// // create RSA encryption key
	// jwk = testkms.createKeyRSAOAEP("unittest_key_3");
	// assertEquals("Key created with correct type","RSA", jwk.getKeyType());
	// assertEquals("Key created with correct use",org.jose4j.jwk.Use.ENCRYPTION,
	// jwk.getUse());
	// assertEquals("Key created with correct algorithm","RSA-OAEP",
	// jwk.getAlgorithm());
	//
	// // create an encryption
	// jwe = new JsonWebEncryption();
	// jwe.setPlaintext(plaintext);
	// jwe.setKey(jwk.getKey());
	// jwe.setKeyIdHeaderValue(jwk.getKeyId());
	// jwe.setAlgorithmHeaderValue(jwk.getAlgorithm());
	// jwe.setEncryptionMethodHeaderParameter("A256GCM");
	// ciphertext = jwe.getCompactSerialization();
	//
	// // perform decryption
	// jwe.setCompactSerialization(ciphertext);
	// jwe.setKey(testkms.getKeyByID(jwe.getKeyIdHeaderValue()).getKey());
	// assertEquals("RSA decryption performed okay", plaintext,
	// jwe.getPlaintextString());
	//
	// // create RSA signing key
	// jwk = testkms.createKeyRS256("unittest_key_4");
	// assertEquals("Key created with correct type","RSA", jwk.getKeyType());
	// assertEquals("Key created with correct use",org.jose4j.jwk.Use.SIGNATURE,
	// jwk.getUse());
	// assertEquals("Key created with correct algorithm", "RS256",
	// jwk.getAlgorithm());
	//
	//
	// // create a signature
	// jws = new JsonWebSignature();
	// PublicJsonWebKey rjws = (PublicJsonWebKey)jwk;
	// jws.setKey(rjws.getPrivateKey());
	// jws.setKeyIdHeaderValue(jwk.getKeyId());
	// jws.setAlgorithmHeaderValue(jwk.getAlgorithm());
	// ciphertext = jws.getCompactSerialization();
	//
	// // verify a signature
	// jws = new JsonWebSignature();
	// jws.setCompactSerialization(ciphertext);
	// jws.setKey(testkms.getKeyByID(jws.getKeyIdHeaderValue()).getKey());
	// assertTrue("RS256 verification performed okay", jws.verifySignature());
	//
	// assertEquals("KMS now contains four keys",
	// 4,testkms.jwkSet.getJsonWebKeys().size());
	//
	// testkms.deleteKeyByID("unittest_key_1");
	//
	// assertNull("Deleted key unittest_key_1",
	// testkms.getKeyByID("unittest_key_1"));
	// assertEquals("Deleted a key", 3,testkms.jwkSet.getJsonWebKeys().size());
	// }

}
