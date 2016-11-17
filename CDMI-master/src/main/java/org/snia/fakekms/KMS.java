package org.snia.fakekms;

import java.io.*;
import java.nio.file.Files;

import java.security.spec.ECParameterSpec;

import java.util.List;

import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.OctJwkGenerator;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.lang.JoseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstration stub that emulates a key server
 */
public class KMS {

    /**
     * Declares the filename used to hold the keyfile
     */
    private final static String KEYFILENAME = "key.json";

    /**
     * Hold the singleton instance of this object
     */
    private static KMS instance;

    /**
     * Contains all the JWK in the KMS
     */
    protected JsonWebKeySet jwkSet = null;

    /**
     * Contains the logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(KMS.class);

    /**
     * Holds a File handle pointing to the keyfile
     */
    private final File keyFile;

    /**
     * Contains the EC keypair generator
     */
    private final ECParameterSpec ecSpec;

    /**
     * Constructor. Should not be called directly but via getInstance().
     *
     */
    private KMS() {

        // initialize local variables
        keyFile = new File(KEYFILENAME);

        // initialize EC key generator
        ecSpec = EllipticCurves.getSpec("P-256");
        this.readKeyFile();

        // handle initialization case where file could not be read but we still
        // need to continue.
        if (this.jwkSet == null) {
            this.jwkSet = new JsonWebKeySet();
        }
    }

    public synchronized JsonWebKey addKeyFromString(String jwkstring)
            throws KMSKeyErrorException, KMSKeyExistsException {
        this.readKeyFile();

        JsonWebKey jwk;

        try {
            jwk = JsonWebKey.Factory.newJwk(jwkstring);
        } catch (JoseException ex) {
            logger.error("Could not parse JWK string");

            throw new KMSKeyErrorException("Could not parse JWK string", ex);
        }

        String kid = jwk.getKeyId();

        if ((kid == null) || (kid == "")) {
            logger.error("Key does not have a key id");

            throw new KMSKeyErrorException("Key does not have a key id.");
        }

        if (this.getKeyByID(kid) != null) {
            logger.error("A key with this key id already exists in the KMS.");

            throw new KMSKeyExistsException("A key with this key id already exists in the KMS.");
        }

        jwkSet.addJsonWebKey(jwk);
        logger.info("Succesfully added key with key id '" + kid + "'.'");
        logger.debug(jwk.toJson());
        this.writeKeyFile();

        return jwk;
    }

    /**
     * Removes all keys in the KMS. Use with care!!
     */
    public synchronized void cleanKMS() {
        this.jwkSet = new JsonWebKeySet();
        this.writeKeyFile();
    }

    /**
     * Create a new JWK containing a symmetric 256 bit AES key for use with the
     * A256KW encryption algorithm.
     *
     * @param kid The Key ID of the key to generate.
     * @return a new JWK containing the generated key.
     * @throws KMSKeyExistsException when a key with this id already exists.
     */
    public synchronized JsonWebKey createKeyA256KW(String kid) throws KMSKeyExistsException {

        // Check if key already exists
        if (this.getKeyByID(kid) != null) {
            throw new KMSKeyExistsException("A key with kid " + kid + "already exists within the KMS.");
        }

        JsonWebKey newJwk;

        // make sure our list is up to date.
        this.readKeyFile();

        // Generate a random AES key
        newJwk = OctJwkGenerator.generateJwk(256);
        newJwk.setKeyId(kid);
        newJwk.setAlgorithm("A256KW");
        newJwk.setUse(org.jose4j.jwk.Use.ENCRYPTION);

        // Add it to the keylist
        jwkSet.addJsonWebKey(newJwk);
        logger.info("Successfully created a new AES key with key ID '" + newJwk.getKeyId() + "'");
        logger.debug(newJwk.toJson());

        // Write the keylist back to file.
        this.writeKeyFile();

        return newJwk;
    }


    /**
     * Create a new JWK containing a symmetric 256 bit AES key for use with the
     * HS256 signing algorithm.
     *
     * @param kid The Key ID of the key to generate.
     * @return a new JWK containing the generated key.
     * @throws KMSKeyExistsException when a key with this id already exists.
     */
    public synchronized JsonWebKey createKeyHS256(String kid) throws KMSKeyExistsException {

        // Check if key already exists
        if (this.getKeyByID(kid) != null) {
            throw new KMSKeyExistsException("A key with kid " + kid + "already exists within the KMS.");
        }

        JsonWebKey newJwk;

        // make sure our list is up to date.
        this.readKeyFile();

        // Generate a random AES key
        newJwk = OctJwkGenerator.generateJwk(256);
        newJwk.setKeyId(kid);
        newJwk.setAlgorithm("HS256");
        newJwk.setUse(org.jose4j.jwk.Use.SIGNATURE);

        // Add it to the keylist
        jwkSet.addJsonWebKey(newJwk);
        logger.info("Successfully created a new AES key with key ID '" + newJwk.getKeyId() + "'");
        logger.debug(newJwk.toJson());

        // Write the keylist back to file.
        this.writeKeyFile();

        return newJwk;
    }

    /**
     * Create a new JWK containing a asymmetric 2048 bit RSA key for use with
     * the RS256 signature algorithm.
     *
     * @param kid The Key ID of the key to generate.
     * @return a new JWK containing the generated key.
     * @throws KMSKeyExistsException when a key with this id already exists.
     */
    public synchronized JsonWebKey createKeyRS256(String kid) throws KMSKeyExistsException {

        // Check if key already exists
        if (this.getKeyByID(kid) != null) {
            throw new KMSKeyExistsException("A key with kid " + kid + " already exists within the KMS.");
        }

        JsonWebKey newJwk = null;

        try {

            // make sure our list is up to date.
            this.readKeyFile();

            // Generate a random RSA key
            newJwk = RsaJwkGenerator.generateJwk(2048);
            newJwk.setKeyId(kid);
            newJwk.setUse(org.jose4j.jwk.Use.SIGNATURE);
            newJwk.setAlgorithm("RS256");

            // Add it to the keylist
            jwkSet.addJsonWebKey(newJwk);
            logger.info("Successfully created a new RSA key with key ID '" + newJwk.getKeyId() + "'");
            logger.debug(newJwk.toJson());

            // Write the keylist back to file.
            this.writeKeyFile();
        } catch (JoseException ex) {
            logger.error("Key could not be generated: " + KEYFILENAME, ex);
        }

        return newJwk;
    }

    /**
     * Create a new JWK containing a asymmetric 2048 bit RSA key for use with
     * the RSA-OAEP encryption algorithm.
     *
     * @param kid The Key ID of the key to generate.
     * @return a new JWK containing the generated key.
     * @throws KMSKeyExistsException when a key with this id already exists.
     */
    public synchronized JsonWebKey createKeyRSAOAEP(String kid) throws KMSKeyExistsException {

        // Check if key already exists
        if (this.getKeyByID(kid) != null) {
            throw new KMSKeyExistsException("A key with kid " + kid + " already exists within the KMS.");
        }

        JsonWebKey newJwk = null;

        try {

            // make sure our list is up to date.
            this.readKeyFile();

            // Generate a random RSA key
            newJwk = RsaJwkGenerator.generateJwk(2048);
            newJwk.setKeyId(kid);
            newJwk.setUse(org.jose4j.jwk.Use.ENCRYPTION);
            newJwk.setAlgorithm("RSA-OAEP");

            // Add it to the keylist
            jwkSet.addJsonWebKey(newJwk);
            logger.info("Successfully created a new RSA key with key ID '" + newJwk.getKeyId() + "'");
            logger.debug(newJwk.toJson());

            // Write the keylist back to file.
            this.writeKeyFile();
        } catch (JoseException ex) {
            logger.error("Key could not be generated: " + KEYFILENAME, ex);
        }

        return newJwk;
    }

    /**
     * delete a single key from the KMS
     *
     * @param kid Key ID of the key to be removed.
     */
    public synchronized void deleteKeyByID(String kid) {

        // make sure the keylist is up to date
        this.readKeyFile();

        // find keys matching the id
        List<JsonWebKey> keysfound = jwkSet.findJsonWebKeys(kid, null, null, null);

        // actually remove them from the keyset
        jwkSet.getJsonWebKeys().removeAll(keysfound);
        logger.info("Deleted " + keysfound.size() + " keys from the KMS with kid " + kid + ".");
        this.writeKeyFile();
    }

    /**
     * Reads the JSON keyfile and merges with the internal keylist, overwriting
     * any existing keys.
     */
    private synchronized void readKeyFile() {
        try {

            // read the JSON keyfile
           String json = new String(Files.readAllBytes(keyFile.toPath()));

            // parse the file into a JWKset
            this.jwkSet = new JsonWebKeySet(json);
            logger.debug("Parsed " + jwkSet.getJsonWebKeys().size() + " keys from file: " + keyFile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            logger.warn("Key file could not be found: " + keyFile.getAbsolutePath(), ex);
        } catch (JoseException ex) {
            logger.warn("Key file could not be parsed: " + keyFile.getAbsolutePath(), ex);
        } catch (IOException ex) {
            logger.warn("Key file could not be found: " + keyFile.getAbsolutePath(), ex);
        }
    }

    /**
     * Write the internal keylist to the JSON keyfile, overwriting any changes
     * in that file.
     */
    private synchronized void writeKeyFile() {
        try {

            // serialize the keylist into json
            String json = jwkSet.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);

            // write the result to the file            
            FileWriter fw = new FileWriter(keyFile);
            fw.write(json);
            fw.close();

            logger.debug("written " + jwkSet.getJsonWebKeys().size() + " keys to file: " + keyFile.getAbsolutePath());
        } catch (IOException ex) {
            logger.error("Key file could not be written: " + KEYFILENAME, ex);
        }
    }

    /**
     * returns either a new or an existing instance of the KMS
     *
     * @return an instance of the KMS
     */
    public synchronized static KMS getInstance() {
        if (instance == null) {
            instance = new KMS();
        }

        return instance;
    }

    /**
     * Finds a specific key in the KMS
     *
     * @param kid Key ID of the key to search for
     * @return a key with the named key ID, or null if no key was found.
     */
    public synchronized JsonWebKey getKeyByID(String kid) {
        JsonWebKey jwk = jwkSet.findJsonWebKey(kid, null, null, null);

        if (jwk == null) {
            logger.info("Key with ID '" + kid + "' was not found.");
        } else {
            logger.debug("Key with ID ID '" + kid + "' was found. Type is '" + jwk.getKeyType() + "'.");
        }

        return jwk;
    }
}
