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

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snia.cdmiserver.dao.filesystem.ContainerDaoImpl;
import org.snia.cdmiserver.exception.BadRequestException;

/**
 * <p>
 * Representation of a CDMI <em>Container</em>.
 * </p>
 */
public class Container {

    private static final Logger LOG = LoggerFactory.getLogger(Container.class);

    // Container creation fields
    private Map<String, String> metadata = new HashMap<String, String>();
    private Map<String, Object> exports = new HashMap<String, Object>();
    private String copy;
    private String move;
    private String reference;
    private String snapshot; // To create a snapshot via the "update" operation

    // Container representation fields
    private String objectType;
    private String objectID;
    private String parentURI;
    private String domainURI;
    private String capabilitiesURI;
    private String completionStatus;
    private Integer percentComplete; // FIXME - Specification says String but that does not make
    // sense
    private List<String> snapshots = new ArrayList<String>();
    private String childrenrange;
    private List<String> children = new ArrayList<String>();
    private String objectName;

    // Representation also includes "metadata", "exports"
    // Representation also includes "mimetype", "metadata", and "value" from creation fields
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public Map<String, Object> getExports() {
        return exports;
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

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
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

    public String getDomainURI() {
        return domainURI;
    }

    public void setDomainURI(String domainURI) {
        this.domainURI = domainURI;
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

    public List<String> getSnapshots() {
        return snapshots;
    }

    public String getChildrenrange() {
        return childrenrange;
    }

    public void setChildrenrange(String childrenrange) {
        this.childrenrange = childrenrange;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setMetaData(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String toJson(boolean toFile) {

        try {
            JSONObject j = new JSONObject();

            //
            if (objectType != null) {
                j.put("objectID", objectID);
            }

            if (objectName != null) {
                j.put("objectName", objectName);
            }

            if (capabilitiesURI != null) {
                j.put("capabilitiesURI", capabilitiesURI);
            }

            if (domainURI != null) {
                j.put("domainURI", domainURI);
            }

            JSONObject m = new JSONObject();
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                m.put(entry.getKey(), entry.getValue());
            }
            j.put("metadata", m);

            // TODO: exports are not properly defined, and not properly parsed
            JSONObject x = new JSONObject();
            for (Map.Entry<String, Object> entry : exports.entrySet()) {
                x.put(entry.getKey(), entry.getValue().toString());
            }
            j.put("exports", x);

            if (!toFile) {

                if (objectType != null) {
                    j.put("objectType", objectType);
                }

                if (parentURI != null) {
                    j.put("parentURI", parentURI);
                }

                JSONArray c = new JSONArray();
                for (String entry : children) {
                    c.add(entry);
                }
                j.put("children", c);

                if (childrenrange != null) {
                    j.put("childrenrange", childrenrange);
                }

                if (completionStatus != null) {
                    j.put("completionStatus", completionStatus);
                }
            }
            return j.toJSONString(JSONStyle.NO_COMPRESS);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ("Error : " + ex);
        }
    }

    public void fromJson(InputStream jsonIs, boolean fromFile) throws ParseException {
        JSONParser jp = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject json = (JSONObject) jp.parse(jsonIs);
        fromJson(json, fromFile);
    }

    public void fromJson(byte[] jsonBytes, boolean fromFile) throws ParseException {
        JSONParser jp = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject json = (JSONObject) jp.parse(jsonBytes);
        fromJson(json, fromFile);
    }

    private void fromJson(JSONObject json, boolean fromFile) {

        if (json.containsKey("metadata")) {
            JSONObject m = (JSONObject) json.get("metadata");

            for (Map.Entry<String, Object> entry : m.entrySet()) {
                this.getMetadata().put(entry.getKey(), (String) entry.getValue());
            }

        }

        if (json.containsKey("exports")) {
            JSONObject x = (JSONObject) json.get("exports");

            for (Map.Entry<String, Object> entry : x.entrySet()) {
                this.getMetadata().put(entry.getKey(), (String) entry.getValue());
            }

        }

        if (json.containsKey("capabilitiesURI")) {
            this.setCapabilitiesURI(json.getAsString("capabilitiesURI"));
        }

        if (json.containsKey("domainURI")) {
            this.setDomainURI(json.getAsString("domainURI"));
        }

        if (json.containsKey("objectName")) {
            this.setObjectName(json.getAsString("objectName"));
        }

        if (json.containsKey("move")) {
            this.setMove(json.getAsString("Move"));
        }

        if (fromFile) { // accept rest of key-values
            if (json.containsKey("objectID")) {
                this.setObjectID(json.getAsString("setObjectID"));
            }

        }
    }

    public Object getObjectURI() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
