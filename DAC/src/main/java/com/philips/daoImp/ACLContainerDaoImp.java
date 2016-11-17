package com.philips.daoImp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.ws.rs.BadRequestException;

import com.philips.dao.ACLContainerDao;
import com.philips.model.ACLContainer;

public class ACLContainerDaoImp implements ACLContainerDao {
	private String baseDirectoryName;

	public String getBaseDirectoryName() {
		return baseDirectoryName;
	}

	public void setBaseDirectoryName(String baseDirectoryName) {
		this.baseDirectoryName = baseDirectoryName;
	}

	public void createDacContainer(String path, ACLContainer container) {
		// TODO Auto-generated method stub
		String dacContainerDirectory = baseDirectoryName + "/" + path;

		String dacContainerMetadataFile = baseDirectoryName + "/"
				+ getDacContainerMetadataPath(path);
		File dacContainerFile = new File(dacContainerDirectory);
		if (!dacContainerFile.exists()) {
			if (!dacContainerFile.mkdir()) {
				throw new IllegalArgumentException("Cannot create container '"
						+ path + "'");
			}
		}

		try {
			FileWriter fw = new FileWriter(dacContainerMetadataFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(container.toJSON());
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new IllegalArgumentException(
					"Cannot write container fields file @" + path + " error : "
							+ e);
		}

	}

	public String getDacContainerMetadataPath(String path) {
		String[] tokens = path.split("[/]+");
		if (tokens.length < 1) {
			throw new BadRequestException("No object name in path <" + path
					+ ">");
		}
		String parentDirectory = "";
		String MetadataFileName = "." + tokens[tokens.length - 1];
		for (int i = 0; i < tokens.length - 1; i++) {
			parentDirectory += tokens[i] + "/";
		}

		return parentDirectory + MetadataFileName;

	}

	public ACLContainer getDacContainer(String path) {
		// TODO Auto-generated method stub
		String dacContianerMetadataFile = baseDirectoryName + "/"
				+ getDacContainerMetadataPath(path);
		ACLContainer container = new ACLContainer();
		FileInputStream in;
		try {
			in = new FileInputStream(dacContianerMetadataFile);
			int inpSize = in.available();
			byte[] inBytes = new byte[inpSize];
			in.read(inBytes);
			container.fromJSON(inBytes);
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return container;
	}

	public void deleteDacContainer(String path) {

		String dacContainerDirectory = baseDirectoryName + "/" + path;
		File file = new File(dacContainerDirectory);
		deleteDacContainerDirectory(file);
		file.delete();
		File metadata = new File(baseDirectoryName + "/"
				+ getDacContainerMetadataPath(path));
		metadata.delete();
	}

	// just delete the container directory not include the metadata
	private void deleteDacContainerDirectory(File file) {
		if (!file.isDirectory()) {
			file.delete();
		} else {
			File[] subFiles = file.listFiles();
			for (File f : subFiles) {
				deleteDacContainerDirectory(f);
				f.delete();
			}

		}

	}

	// test container
	public static void main(String[] args) {

		// //test create
		// ACLContainerDaoImp aclcdao = new ACLContainerDaoImp();
		// aclcdao.baseDirectoryName = "c:/data";
		//
		//
		// ACLContainer aclc = new ACLContainer();//
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
		// aclc.setAcl(Arrays.asList(aces));
		//
		// aclcdao.createDacContainer("testdacContainer", aclc);

		// test delete
		ACLContainerDaoImp aclcdao = new ACLContainerDaoImp();
		aclcdao.baseDirectoryName = "c:/data";

		aclcdao.deleteDacContainer("TestContainer/DACContainer/");

	}

}
