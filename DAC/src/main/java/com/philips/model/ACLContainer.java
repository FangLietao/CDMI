package com.philips.model;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

public class ACLContainer {
	private String containerID;
	private String containerName;
	private List<ACEntity> acl = new ArrayList<ACEntity>();

	public String getContainerID() {
		return containerID;
	}

	public void setContainerID(String containerID) {
		this.containerID = containerID;
	}

	public String getContainerName() {
		return containerName;
	}

	public List<ACEntity> getAcl() {
		return acl;
	}

	public void setAcl(List<ACEntity> acl) {
		this.acl = acl;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	// transfer object to json_string
	public String toJSON() {
		StringWriter buffer = new StringWriter();
		JsonFactory jfactory = new JsonFactory();
		try {
			JsonGenerator g = jfactory.createJsonGenerator(buffer);
			g.writeStartObject();
			g.writeRaw("\n");
			g.writeRaw("\t");

			if (this.containerID != null) {
				g.writeStringField("ContainerID", containerID);
				g.writeRaw("\n");
				g.writeRaw("\t");
			}
			if (this.containerName != null) {
				g.writeStringField("objectName", containerName);
				g.writeRaw("\n");
				g.writeRaw("\t");
			}
			if (this.acl != null) {

				g.writeArrayFieldStart("dac_acl");
				g.writeRaw("\n");
				g.writeRaw("\t\t");
				Iterator<ACEntity> it = acl.iterator();

				while (it.hasNext()) {
					g.writeStartObject();
					ACEntity ace = (ACEntity) it.next();
					g.writeStringField("acetype", ace.getAceType());
					g.writeRaw("\n");
					g.writeRaw("\t\t");
					g.writeStringField("identifier", ace.getIdentifier());
					g.writeRaw("\n");
					g.writeRaw("\t\t");
					g.writeStringField("aceflags", ace.getAceFlags());
					g.writeRaw("\n");
					g.writeRaw("\t\t");
					g.writeStringField("acemask", ace.getAceMask());
					g.writeRaw("\n");
					g.writeRaw("\t\t");
					g.writeEndObject();
				}

				g.writeEndArray();

			}
			g.writeRaw("\n");
			g.writeEndObject();
			g.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buffer.toString();
	}

	// transfer json_string to object
	public void fromJSON(byte[] bytes) throws JsonParseException, IOException {
		if (bytes.length == 0)
			return;
		JsonFactory f = new JsonFactory();
		JsonParser jp = f.createJsonParser(bytes);
		jp.nextToken();
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			String key = jp.getCurrentName();
			if ("containerName".equals(key)) {
				jp.nextToken();
				this.containerName = jp.getText();
			}
			if ("containerID".equals(key)) {
				jp.nextToken();
				this.containerID = jp.getText();
			}
			if ("dac_acl".equals(key)) {
				jp.nextToken(); // [
				while (jp.nextToken() != JsonToken.END_ARRAY) { // {
					ACEntity ace = new ACEntity();
					while (jp.nextToken() != JsonToken.END_OBJECT) {
						// jp.nextToken();
						String acekey = jp.getCurrentName();
						if (acekey.equals("acetype")) {

							ace.setAceType(jp.getText());
						}
						if (acekey.equals("identifier")) {

							ace.setIdentifier(jp.getText());
						}
						if (acekey.equals("aceflags")) {

							ace.setAceFlags(jp.getText());
						}
						if (acekey.equals("acemask")) {

							ace.setAceMask(jp.getText());
						}

					}
					acl.add(ace);
				}
			}

		}

	}
}
