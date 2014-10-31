package com.redsqirl.workflow.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import com.idiro.hadoop.NameNodeVar;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;

/**
 * Manage installation and uninstallation of SuperAction.
 * 
 * @author etienne
 *
 */
public class SuperActionManager {

	private static Logger logger = Logger.getLogger(SuperActionManager.class);
	
	public String export(String user ,SubDataFlow toExport , Boolean privilage) throws RemoteException{
		String error = null;
		String filePath ="/user/"+user+"/redsqirl-save/"+toExport.getName();
		error = toExport.save(filePath, privilage);
		return error;
	}
	
	public String importSA(String user,String pathHdfs) throws IOException{
		String error = null;
		FileSystem fs = NameNodeVar.getFS();
		Path path = new Path(pathHdfs);
		Path dest = new Path(getSuperActionMainDir(user).getAbsolutePath());
		if(fs.isFile(path)){
			try{
				fs.copyToLocalFile(path, dest);
				String filename = pathHdfs.substring(pathHdfs.lastIndexOf("/"));
				String filenameReplced = filename.substring(0,filename.indexOf(".srs"));
				File file = new File(getSuperActionMainDir(user).getAbsolutePath()+filename);
				File file2 = new File(getSuperActionMainDir(user).getAbsolutePath()+filenameReplced);
				
				file.renameTo(file2);
				
			} catch (Exception e ){
				error ="Problem transfering "+pathHdfs+" on HDFS to "+ dest.getName()+" on local filesystem";
			}
		}else{
			error = "Propelem with file :"+path;
		}
		return error;
		
	}
	
	public String install(String user, SubDataFlow toInstall, Boolean privilege)
			throws RemoteException {
		String name = toInstall.getName();
		File mainDir = getSuperActionMainDir(user);
		mainDir.mkdirs();
		File mainFile = new File(mainDir, name);
		String error = null;
		if (mainFile.exists()) {
			error = "Super Action '"
					+ name
					+ "' already exist, please rename this object or uninstall the super action before trying again.";
		} else {
			logger.debug("Check installation file");
			error = toInstall.check();
		}

		if (error == null) {
			logger.debug("Save main file into: " + mainFile.getPath());
			error = toInstall.saveLocal(mainFile, privilege);
			
			if (error != null) {
				mainFile.delete();
			} else {
				File helpDir = getSuperActionHelpDir(user);
				helpDir.mkdirs();
				File helpFile = new File(helpDir, name + ".html");
				logger.debug("Save help into: " + helpFile.getPath());
				String helpContent = toInstall.buildHelpFileContent();
				try {
					FileWriter fw = new FileWriter(helpFile);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(helpContent);
					bw.close();

				} catch (IOException e) {
					error = "Fail to write the help file";
					logger.error(error + ": " + e.toString(), e);
				}

				if (error != null) {
					mainFile.delete();
					helpFile.delete();
				}
			}
		}

		return error;
	}

	public List<String> getAvailableSuperActions(String user) {
		File sysSA = getSuperActionMainDir(null);
		File userSA = getSuperActionMainDir(user);
		// final String pattern= "^[a-zA-Z0-9]*$";
		final String pattern = "sa_[a-zA-Z0-9]*";

		List<String> ansL = new LinkedList<String>();
		try {
			if (sysSA.exists()) {
				ansL.addAll(Arrays.asList(sysSA.list(new FilenameFilter() {

					@Override
					public boolean accept(File arg0, String name) {
						return name.matches(pattern) && name.startsWith("sa_");
					}
				})));
			}
		} catch (Exception e) {
			logger.error("error ", e);
		}
		try {
			if (userSA.exists()) {
				ansL.addAll(Arrays.asList(userSA.list(new FilenameFilter() {

					@Override
					public boolean accept(File arg0, String name) {
						return name.matches(pattern) && name.startsWith("sa_");
					}
				})));
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return ansL;

	}

	public String uninstall(String user, String name) {
		File mainFile = new File(getSuperActionMainDir(user), name);
		File helpFile = new File(getSuperActionHelpDir(user), name + ".html");

		mainFile.delete();
		helpFile.delete();

		return null;
	}

	public File getSuperActionMainDir(String user) {
		File ans = null;
		if (user != null) {
			ans = new File(WorkflowPrefManager.getPathUserSuperAction(user));
		} else {
			ans = new File(WorkflowPrefManager.getPathSysSuperAction());
		}

		logger.info("Super action path for "+(user == null? "sys":user)+": "+ans.getPath());
		
		return ans;
	}

	public File getSuperActionHelpDir(String user) {
		String tomcatpath = WorkflowPrefManager
				.getSysProperty(WorkflowPrefManager.sys_tomcat_path);
		String installPackage = WorkflowPrefManager.getSysProperty(
				WorkflowPrefManager.sys_install_package, tomcatpath);
		logger.debug("Install Package in: " + installPackage);
		return user == null || user.isEmpty() ? new File(installPackage
				+ WorkflowPrefManager.getPathSysHelpPref()) : new File(
				installPackage + WorkflowPrefManager.getPathUserHelpPref(user));
	}

}
