package com.philips.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minidev.json.parser.ParseException;

import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;

import com.philips.dacHttp.DacRequestEntity;
import com.philips.dacHttp.DacResponseEntity;
import com.philips.dacHttp.ObjectKey;
import com.philips.dacHttp.SecurityDacRequestEntity;
import com.philips.dao.ACLContainerDao;
import com.philips.dao.ACLObjectDao;
import com.philips.model.ACEntity;
import com.philips.model.ACLObject;
import com.philips.util.Identifier;

public class AuthorityService {
	private  ACLObjectDao aclObjectDao;
	private  ACLContainerDao aclContainerDao;

	public ACLContainerDao getAclContainerDao() {
		return aclContainerDao;
	}

	public void setAclContainerDao(ACLContainerDao aclContainerDao) {
		this.aclContainerDao = aclContainerDao;
	}

	public ACLObjectDao getAclObjectDao() {
		return aclObjectDao;
	}

	public void setAclObjectDao(ACLObjectDao aclObjectDao) {
		this.aclObjectDao = aclObjectDao;
	}

	public DacResponseEntity getAccessAuthorition(byte[] bytes) {
		DacResponseEntity responseEntity = new DacResponseEntity();
		try {
			SecurityDacRequestEntity securityReqEntity = new SecurityDacRequestEntity(
					new String(bytes));
			String jws = securityReqEntity
					.sigVertifyDacRequestEntity(securityReqEntity
							.getDac_request());
			String jwe = securityReqEntity.decryptDacResponseEntity(jws);
			DacRequestEntity reqEntity = new DacRequestEntity(jwe);

			// find the acl object by cdmi object id
			ACLObject aclObj = aclObjectDao.getACL(reqEntity.getCdmiObjectId());

			if (aclObj == null) {
				// if the acl not exist,create a defalut acl of the obj
				aclObj = new ACLObject();
				aclObj.setObjID(reqEntity.getCdmiObjectId());
				ACEntity aclEntity = new ACEntity();
				aclEntity.setIdentifier("EVERYONE@");
				aclEntity.setAceType("DENY");
				aclEntity.setAceFlags("0x00000000");
				aclEntity.setAceMask("0x00000000");
				List<ACEntity> objACL = new ArrayList<ACEntity>();
				objACL.add(aclEntity);
				aclObj.setObjACL(objACL);

				aclObjectDao.createACL(reqEntity.getCdmiObjectId(), aclObj);

				responseEntity.withDacAppliedMask(aclEntity.getAceMask());
			} else {
				List<ACEntity> acl = aclObj.getObjACL();
				int i;
				for (i = 0; Identifier
						.transToIdentifier(((ACEntity) acl.get(i))
								.getIdentifier()) < Identifier
						.transToIdentifier(reqEntity.getClientIdentity().get(
								"acl_group"))
						&& i < acl.size(); i++) {
				}
				if (i == acl.size()) {
					ACEntity aclEntity = new ACEntity();
					aclEntity.setIdentifier("EVERYONE@");
					aclEntity.setAceType("ALLOW");
					aclEntity.setAceFlags("0x00000000");
					aclEntity.setAceMask("0x00000000");
					acl.add(aclEntity);
				}
				responseEntity.withDacAppliedMask(((ACEntity) acl.get(i))
						.getAceMask());
			}

			JsonWebKey objKey = ObjectKey.getObjectKey((reqEntity
					.getCdmiEncKeyId()));
			responseEntity.withDacObjectKey(objKey).withDacResponseId(
					UUID.randomUUID().toString());

		} catch (ParseException | JoseException e) {

			e.printStackTrace();
		}

		return responseEntity;

	}

}
