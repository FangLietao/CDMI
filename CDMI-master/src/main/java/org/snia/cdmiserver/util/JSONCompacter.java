package org.snia.cdmiserver.util;

import java.util.HashMap;
import net.minidev.json.JSONObject;
import org.jose4j.jwx.CompactSerializer;

/**
 *
 * @author 310183272
 */
public class JSONCompacter {

    
    static public JSONObject CompactJWEToJSON(String compactJWE) {

        String[] parts = CompactSerializer.deserialize(compactJWE);

        // this is: getEncodedHeader(), encodedEncryptedKey, encodedIv, encodedCiphertext, encodedTag
        HashMap<String, String> hm = new HashMap<>();

        hm.put("protected", parts[0]);
        hm.put("encrypted_key", parts[1]);
        hm.put("iv", parts[2]);
        hm.put("ciphertext", parts[3]);
        hm.put("tag", parts[4]);

        JSONObject json = new JSONObject(hm);

        return json;
    }

    static public String JSONToCompactJWE(JSONObject json) {

        return CompactSerializer.serialize(
                json.getAsString("protected"), 
                json.getAsString("encrypted_key"),
                json.getAsString("iv"), 
                json.getAsString("ciphertext"), 
                json.getAsString("tag")
        );
    }

}
