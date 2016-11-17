package com.philips.daoImp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import com.philips.dao.ACLObjectDao;
import com.philips.model.ACLObject;

public class ACLObjectDaoImp implements ACLObjectDao {

	private String baseDirectoryName;

	public String getBaseDirectoryName() {
		return baseDirectoryName;
	}

	public void setBaseDirectoryName(String baseDirectoryName) {
		this.baseDirectoryName = baseDirectoryName;
	}

	public void createACL(String path, ACLObject obj) {
		// TODO Auto-generated method stub
		String dacObjectFile = baseDirectoryName + "/" + path;

		try {
			FileWriter fw = new FileWriter(dacObjectFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(obj.toJSON());
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void deleteACL(String path) {
		// TODO Auto-generated method stub
		String dacObjectFile = baseDirectoryName + "/" + path;
		File dacFile = new File(dacObjectFile);
		dacFile.delete();

	}

	public ACLObject getACL(String path) {
		// TODO Auto-generated method stub
		String dacObjectFile = baseDirectoryName + "/" + path;
		ACLObject obj = new ACLObject();
		FileInputStream in;
		try {
			in = new FileInputStream(dacObjectFile);
			int inpSize = in.available();
			byte[] inBytes = new byte[inpSize];
			in.read(inBytes);
			obj.fromJSON(inBytes);
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return obj;
	}

	public ACLObject updateACL(ACLObject obj, ACLObject dobj) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// test create
		// ACLObject acl = new ACLObject();
		// ACEntity[] aces = new ACEntity[3];
		// ACEntity a1 = new ACEntity();
		// ACEntity a2 = new ACEntity();
		// ACEntity a3 = new ACEntity();
		// a1.setAceFlags("1");
		// a1.setAceFlags("2");
		// a1.setAceFlags("3");
		// a1.setAceFlags("4");
		// aces[0] = a1;
		// aces[1] = a2;
		// aces[2] = a3;
		// acl.setAcl(Arrays.asList(aces));
		// System.out.println(acl.toJSON(acl));
		// ACLObjectDaoImp aclObjDao=new ACLObjectDaoImp();
		// aclObjDao.createACL("c:/data/acl", acl);

		// test get
		 ACLObjectDaoImp aclObjDao=new ACLObjectDaoImp();
		 aclObjDao.setBaseDirectoryName("c:/data");
		 ACLObject a=aclObjDao.getACL("dac.txt");
		 if(a!=null){
			 System.out.println(a.toJSON());
		 }
		 else{
			 System.out.println("a is null!");
		 }
		 

		// test delete
//		ACLObjectDaoImp aclObjDao = new ACLObjectDaoImp();
//		aclObjDao.deleteACL("c:/data/acl");

	}

}
