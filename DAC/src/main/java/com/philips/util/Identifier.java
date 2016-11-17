package com.philips.util;

public class Identifier {
	public final static int owner = 1;
	public final static int authenticated = 2;
	public final static int group = 3;
	public final static int anonymous = 4;
	public final static int everyone = 5;

	public static int transToIdentifier(String aclIdentity) {
		int identifier = 0;
		switch (aclIdentity) {
		case "OWNER@":
			identifier = 1;
			break;
		case "AUTHENTICATED":
			identifier = 2;
			break;
		case "GROUP@":
			identifier = 3;
			break;
		case "ANONYMOUS@":
			identifier = 4;
			break;
		case "EVERYONE@":
			identifier = 5;
			break;
		}

		return identifier;

	}

}
