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

public class ACLObject {
	private String ObjID;
	private String ObjName;
	private List<ACEntity> ObjACL = new ArrayList<ACEntity>();

	public String getObjID() {
		return ObjID;
	}

	public void setObjID(String objID) {
		ObjID = objID;
	}

	public String getObjName() {
		return ObjName;
	}

	public void setObjName(String objName) {
		ObjName = objName;
	}

	public List<ACEntity> getObjACL() {
		return ObjACL;
	}

	public void setObjACL(List<ACEntity> objACL) {
		ObjACL = objACL;
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
			if (this.ObjID != null) {
				g.writeStringField("objectID", ObjID);
				g.writeRaw("\n");
				g.writeRaw("\t");
			}
			if (this.ObjName != null) {
				g.writeStringField("objectName", ObjName);
				g.writeRaw("\n");
				g.writeRaw("\t");
			}
			if (this.ObjACL != null) {

				g.writeArrayFieldStart("dac_acl");
				Iterator<ACEntity> it = ObjACL.iterator();
				g.writeRaw("\n");
				g.writeRaw("\t\t");
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
			if ("objectName".equals(key)) {
				jp.nextToken();
				this.ObjName = jp.getText();
			}
			if ("objectID".equals(key)) {
				jp.nextToken();
				this.ObjID = jp.getText();
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
					ObjACL.add(ace);
				}
			}

		}
	}

	// test
	/*
	 * public static void main(String[] args) { ACLObject acl = new ACLObject();
	 * acl.setObjName("test"); acl.setObjID("1234"); ACEntity[] aces = new
	 * ACEntity[3]; ACEntity a1 = new ACEntity(); ACEntity a2 = new ACEntity();
	 * ACEntity a3 = new ACEntity(); a1.setAceFlags("1"); a2.setAceFlags("2");
	 * a3.setAceFlags("3");
	 * 
	 * aces[0] = a1; aces[1] = a2; aces[2] = a3;
	 * acl.setAcl(Arrays.asList(aces)); System.out.println(acl.toJSON());
	 * 
	 * ACLObject a=new ACLObject(); try { a.fromJSON(acl.toJSON().getBytes());
	 * System.out.println(a.getObjID()); System.out.println(a.getObjName());
	 * 
	 * } catch (JsonParseException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); } }
	 */

}
