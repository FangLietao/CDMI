package org.snia.cdmiserver.dacHttp;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;

public class DacResponseEntity {

    private String dacResponseVersion = "1";
    private String dacResponseId;
    private String dacIdentity;
    private String dacAppliedMask;
    private JsonWebKey dacObjectKey;
    private Map<String, String> dacResponseHeaders;
    private String dacRedirectObjectId;
    private String dacAuditUri;

    public String getDacResponseVersion() {
        return dacResponseVersion;
    }

    public String getDacResponseId() {
        return dacResponseId;
    }

    public String getDacIdentity() {
        return dacIdentity;
    }

    public String getDacAppliedMask() {
        return dacAppliedMask;
    }

    public JsonWebKey getDacObjectKey() {
        return dacObjectKey;
    }

    public Map getDacResponseHeaders() {
        return dacResponseHeaders;
    }

    public String getDacRedirectObjectId() {
        return dacRedirectObjectId;
    }

    public String getDacAuditUri() {
        return dacAuditUri;
    }

    public DacResponseEntity fromJSONString(byte[] bytes) throws ParseException {
        return fromJSONString(new String(bytes));

    }

    public DacResponseEntity fromJSONString(String jsonStr) throws ParseException {
        JSONParser jp = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jObj = (JSONObject) jp.parse(jsonStr);

        if (jObj.containsKey("dacAppliedMask")) {
            this.dacAppliedMask = jObj.getAsString("dacAppliedMask");
        }
        if (jObj.containsKey("dacAuditUri")) {
            this.dacAuditUri = jObj.getAsString("dacAuditUri");
        }
        if (jObj.containsKey("dacIdentity")) {
            this.dacIdentity = jObj.getAsString("dacIdentity");
        }
        if (jObj.containsKey("dacRedirectObjectId")) {
            this.dacRedirectObjectId = jObj.getAsString("dacRedirectObjectId");
        }
        if (jObj.containsKey("dacResponseId")) {
            this.dacResponseId = jObj.getAsString("dacResponseId");
        }
        if (jObj.containsKey("dacResponseVersion")) {
            this.dacResponseVersion = jObj.getAsString("dacResponseVersion");
        }

        if (jObj.containsKey("dacObjectKey")) {
//            if(jObj.get("dacObjectKey")!=null){
//                JSONParser j= new JSONParser(JSONParser.MODE_PERMISSIVE);
//                JSONObject jKey=(JSONObject) j.parse(jObj.getAsString("dacObjectKey"));
//                this.dacObjectKey.

//        }
            String jsonObjKey = jObj.getAsString("dacObjectKey");
            try {
                dacObjectKey=JsonWebKey.Factory.newJwk(jsonObjKey);
            } catch (JoseException ex) {
                Logger.getLogger(DacResponseEntity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (jObj.containsKey(
                "dacResponseHeaders")) {
            JSONObject jServerIdentity = (JSONObject) jObj.get("dacResponseHeaders");
            dacResponseHeaders = new HashMap();
            for (Map.Entry<String, Object> entry : jServerIdentity.entrySet()) {
                this.dacResponseHeaders.put(entry.getKey(), (String) entry.getValue());
            }
        }

//        if (this.dacAppliedMask != null) {
//            jObj.put("dacAppliedMask", dacAppliedMask);
//        }
//        if (this.dacAuditUri != null) {
//            jObj.put("dacAuditUri", dacAuditUri);
//        }
//        if (this.dacIdentity != null) {
//            jObj.put("dacIdentity", dacIdentity);
//        }
//        if (this.dacRedirectObjectId != null) {
//            jObj.put("dacRedirectObjectId", dacRedirectObjectId);
//        }
//        if (this.dacResponseId != null) {
//            jObj.put(dacResponseId, dacResponseId);
//        }
//        if (this.dacResponseVersion != null) {
//            jObj.put("dacResponseVersion", dacResponseVersion);
//        }
//
//        if (jObj.containsKey("dacObjectKey")) {
// //           JSONObject jServerIdentity = (JSONObject) jObj.get("dacObjectKey");
// //           for (Map.Entry<String, Object> entry : jServerIdentity.entrySet()) {
// //               this.dacObjectKey.put(entry.getKey(), (String) entry.getValue());
// //           }
//              jObj.put("dacObjectKey", dacObjectKey);
//        }
//        
        return this;
    }

}
