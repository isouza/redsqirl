/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.analyticsStore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.ajax4jsf.model.KeepAlive;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redsqirl.BaseBean;
import com.redsqirl.PackageMngBean;
import com.redsqirl.auth.UserInfoBean;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;
import com.redsqirl.workflow.utils.ModelInt;
import com.redsqirl.workflow.utils.ModelManager;
import com.redsqirl.workflow.utils.PackageManager;
import com.redsqirl.workflow.utils.RedSqirlModel;
import com.redsqirl.workflow.utils.RedSqirlPackage;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

@KeepAlive
public class AnalyticsStoreModuleDetailBean extends BaseBean implements Serializable{


	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(AnalyticsStoreModuleDetailBean.class);

	private AnalyticsStoreLoginBean analyticsStoreLoginBean;
	private UserInfoBean userInfoBean;
	private RedSqirlModule moduleVersion;
	private List<RedSqirlModuleVersionDependency> redSqirlModuleVersionDependency;

	private boolean installed;
	private boolean userInstall;

	private String showRestartMSG;

	private List<RedSqirlModule> versionList;

	@PostConstruct
	public void init() {

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String id = params.get("id");
		String version = params.get("version");
		userInstall = params.get("userInstall").equals("true");

		versionList = new ArrayList<RedSqirlModule>();

		try{
			String uri = getRepoServer()+"rest/allpackages";

			JSONObject object = new JSONObject();
			object.put("id", id);
			if (version != null){
				object.put("version", version);
			}
			if(analyticsStoreLoginBean != null && analyticsStoreLoginBean.getEmail() != null){
				object.put("user", analyticsStoreLoginBean.getEmail());
			}

			Client client = Client.create();
			WebResource webResource = client.resource(uri);

			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);

			try{
				JSONArray pckArray = new JSONArray(ansServer);
				for(int i = 0; i < pckArray.length();++i){
					JSONObject pckObj = pckArray.getJSONObject(i);
					RedSqirlModule pck = new RedSqirlModule();
					pck.setId(Integer.valueOf(getString(pckObj, "id")));
					pck.setIdVersion(getString(pckObj, "idVersion"));
					pck.setName(getString(pckObj, "name"));
					pck.setTags(getString(pckObj, "tags"));
					pck.setImage(getRepoServer() + getString(pckObj, "image"));
					pck.setType(getString(pckObj, "type"));
					pck.setVersionNote(getString(pckObj, "versionNote"));
					pck.setHtmlDescription(getString(pckObj, "htmlDescription"));
					pck.setDate(getString(pckObj, "date"));
					pck.setOwnerName(getString(pckObj, "ownerName"));
					pck.setVersionName(getString(pckObj, "versionName"));
					pck.setPrice(getString(pckObj, "price"));
					pck.setValidated(getString(pckObj, "validated"));

					pck.setJson(getString(pckObj, "jsonObject"));

					versionList.add(pck);

					if(pckObj.getString("idVersion").equals(version)){
						moduleVersion = pck;
					}
				}
			} catch (JSONException e){
				logger.warn(e,e);
			}

			if (moduleVersion == null && versionList != null && !versionList.isEmpty()){
				moduleVersion = versionList.get(versionList.size()-1);
			}

			//dependency
			JSONArray jsonArray = new JSONArray(moduleVersion.getJson().substring(moduleVersion.getJson().indexOf("["), moduleVersion.getJson().lastIndexOf("]")+1));
			List<RedSqirlModuleVersionDependency> redSqirlModuleVersionDependencyList = new ArrayList<RedSqirlModuleVersionDependency>();
			for(int j = 0; j < jsonArray.length();++j){
				JSONObject jsonObject = jsonArray.getJSONObject(j);
				RedSqirlModuleVersionDependency redSqirlModuleVersionDependency = new RedSqirlModuleVersionDependency();
				redSqirlModuleVersionDependency.setModuleName(getString(jsonObject, "moduleName"));
				redSqirlModuleVersionDependency.setValueStart(getString(jsonObject, "valueStart"));
				redSqirlModuleVersionDependency.setValueEnd(getString(jsonObject, "valueEnd"));
				redSqirlModuleVersionDependencyList.add(redSqirlModuleVersionDependency);
			}
			setRedSqirlModuleVersionDependency(redSqirlModuleVersionDependencyList);


			String user = null;
			Set<String> packagesInstalled = null;
			PackageManager pckMng = new PackageManager();
			if (userInstall){
				user = userInfoBean.getUserName();
				packagesInstalled = pckMng.getUserPackageNames(user);
			}else{
				packagesInstalled = pckMng.getSysPackageNames();
			}



			if (packagesInstalled.contains(moduleVersion.getName())){
				RedSqirlPackage rs = null;
				if(user == null){
					rs = pckMng.getSysPackage(moduleVersion.getName());
				}else{
					rs = pckMng.getUserPackage(user,moduleVersion.getName());
				}
				String versionPck = rs.getPackageProperty(RedSqirlPackage.property_version);
				if (versionPck.equals(moduleVersion.getVersionName())){
					installed = true;
				}
			}
		}catch(Exception e){
			logger.warn(e,e);
		}
	}

	public void selectVersion(){

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String version = params.get("version");
		for (RedSqirlModule redSqirlModule : versionList) {
			if(redSqirlModule.getIdVersion().equals(version)){
				moduleVersion = redSqirlModule;
			}
		}

	}

	private String getString(JSONObject pckObj, String object) throws JSONException{
		return pckObj.has(object) ? pckObj.getString(object) : "";
	}

	public String installModel() throws ZipException, IOException{
		String downloadUrl = null;
		String fileName = null;
		String key = null;
		String name = null;
		String licenseKeyProperties = null;
		String error = null;

		String softwareKey = getSoftwareKey();

		String user = null;
		if (userInstall){
			user = userInfoBean.getUserName();
		}

		boolean newKey = false;

		try{
			String uri = getRepoServer()+"rest/keymanager";

			JSONObject object = new JSONObject();
			object.put("user", user);
			object.put("key", softwareKey);
			object.put("type", moduleVersion.getType());
			object.put("idModuleVersion", moduleVersion.getIdVersion());
			object.put("installationType", userInstall ? "USER" : "SYSTEM");
			object.put("email", analyticsStoreLoginBean.getEmail());
			object.put("password", analyticsStoreLoginBean.getPassword());

			Client client = Client.create();
			WebResource webResource = client.resource(uri);

			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);

			logger.debug(ansServer);

			try{
				JSONObject pckObj = new JSONObject(ansServer);
				downloadUrl = getRepoServer() + pckObj.getString("url");
				fileName = pckObj.getString("fileName");
				key = pckObj.getString("key");
				name = pckObj.getString("name");
				newKey = pckObj.getBoolean("newKey");
				licenseKeyProperties = pckObj.getString("licenseKeyProperties");
				error = pckObj.getString("error");
			} catch (JSONException e){
				logger.warn(e,e);
			}

		}catch(Exception e){
			logger.warn(e,e);
			error = "msg_error_oops";
		}

		if(error == null || "".equals(error)){

			String tmp = WorkflowPrefManager.pathSysHome;
			String packagePath = tmp + "/tmp/" +fileName;

			logger.debug("packagePath  " + packagePath);

			try {
				URL website = new URL(downloadUrl + "&idUser=" + analyticsStoreLoginBean.getIdUser() + "&key=" + softwareKey);

				logger.debug(downloadUrl + "&idUser=" + analyticsStoreLoginBean.getIdUser() + "&key=" + softwareKey);

				ReadableByteChannel rbc = Channels.newChannel(website.openStream());
				FileOutputStream fos = new FileOutputStream(packagePath);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
			} catch (MalformedURLException e) {
				logger.warn(e,e);
			} catch (IOException e) {
				logger.warn(e,e);
			} 


			if (newKey){

				BufferedWriter writer = null;
				try {
					File file = new File(WorkflowPrefManager.pathSystemLicence);
					String filepath = file.getAbsolutePath();
					if(file.exists()){
						file.delete();
					}
					PrintWriter printWriter = new PrintWriter(new File(filepath));
					printWriter.print(licenseKeyProperties);
					printWriter.close ();
				} catch (Exception e) {
					logger.warn(e,e);
				} finally {
					try {
						writer.close();
					} catch (Exception e) {
					}
				}

			}

			ZipFile zipFile = new ZipFile(packagePath);

			String[] path = zipFile.getFile().getAbsolutePath().split("/");
			String modelName = path[path.length-1].substring(0, path[path.length-1].lastIndexOf('-'));
			ModelManager modelMan = new ModelManager();
			ModelInt model = null;
			if(userInstall){
				model = modelMan.getUserModel(getUserInfoBean().getUserName(), modelName);
			}else{
				model = modelMan.getSysModel(modelName);
			}
			error = model.importModel(zipFile.getFile());
			if(error == null){
				List<SubDataFlow> l = new LinkedList<SubDataFlow>();
				Set<String> lSWN = model.getSubWorkflowNames();
				Iterator<String> it = lSWN.iterator();
				logger.info("Install help for: "+lSWN.toString());
				while(it.hasNext()){
					String cur = it.next();
					logger.info("Init "+cur);
					SubDataFlow sdf = getworkFlowInterface().getNewSubWorkflow();
					sdf.setName(cur);
					sdf.readFromLocal(new File(model.getFile(),cur));
					l.add(sdf);
				}
				logger.info("Call modelInstaller");
				error = modelMan.installModelWebappFiles(model, l);
			}
			if(error == null){
				model.setEditable(false);
			}
			logger.info("Delete zip file");
			File file = new File(packagePath);
			file.delete();



			/*String extractedPackagePath = packagePath.substring(0, packagePath.length()-4);
			logger.debug("extractedPackagePath  " + extractedPackagePath);
			zipFile.extractAll(extractedPackagePath);

			File folder = new File(extractedPackagePath + "/" +fileName.substring(0, fileName.length()-4));
			logger.debug("folder.getPath  " + folder.getPath());

			ModelManager saManager = new ModelManager();
			DataFlowInterface dfi = getworkFlowInterface();

			List<String> curSuperActions = null;
			List<String> nextSuperActions = Arrays.asList(folder.list());
			int iterMax = 20;
			int iter = 0;
			do{
				curSuperActions = nextSuperActions; 
				nextSuperActions = new LinkedList<String>();
				for (String file : curSuperActions){

					logger.debug(file);

					if (file.startsWith("sa_") || file.endsWith(".srs")){

						String workflowName = generateWorkflowName(folder.getPath() + "/" + file);
						dfi.addSubWorkflow(workflowName);

						SubDataFlow swa = dfi.getSubWorkflow(workflowName);

						swa.setName(file.endsWith(".srs") ? file.substring(0, file.length() - 4) : file);

						error = swa.readFromLocal(new File(folder.getPath() + "/" + file));

						if (error == null){
							ModelInt model = null;
							if(userInstall){
								model = saManager.getUserModel(userInfoBean.getUserName(), RedSqirlModel.getModelAndSW(swa.getName())[0]);
							}else{
								model = saManager.getSysModel(RedSqirlModel.getModelAndSW(swa.getName())[0]);
							}
							error = saManager.installSA(model, swa, swa.getPrivilege());
						}

						dfi.removeWorkflow(workflowName);

						if (error != null){
							nextSuperActions.add(file);
							continue;
						}
					}

					if (file.endsWith(".rs")){
						getHDFS().copyFromLocal(folder.getPath() + "/" + file,"/user/"+userInfoBean.getUserName()+"/redsqirl-save/"+file);
					}
				}
				++iter;
			}while(iter < iterMax && ! nextSuperActions.isEmpty() && nextSuperActions.size() < curSuperActions.size());

			File file = new File(packagePath);
			file.delete();

			FileUtils.deleteDirectory(new File(extractedPackagePath));*/

			if (error == null || "".equals(error)){
				MessageUseful.addInfoMessage("Model Installed.");
				installed = true;
			}else{
				MessageUseful.addErrorMessage("Error installing model: " + error);
			}


		}else{
			String value[] = error.split(",");
			if(value.length > 1){
				MessageUseful.addErrorMessage("Error installing model: " + getMessageResourcesWithParameter(value[0],new String[]{value[1]}));
			}else{
				MessageUseful.addErrorMessage("Error installing model: " + getMessageResources(error));
			}
		}

		return "";
	}

	private String generateWorkflowName(String path) {
		String name;
		int index = path.lastIndexOf("/");
		if (index + 1 < path.length()) {
			name = path.substring(index + 1);
		} else {
			name = path;
		}
		return name.replace(".rs", "").replace(".srs", "").replace("sa_", "");
	}

	public String installPackage() throws RemoteException{
		String downloadUrl = null;
		String fileName = null;
		String key = null;
		String name = null;
		String licenseKeyProperties = null;
		String error = null;

		String softwareKey = getSoftwareKey();

		boolean newKey = false;

		String user = null;
		if (userInstall){
			if(userInfoBean.getUserName() != null){
				user = userInfoBean.getUserName();
			}else{
				userInstall = false;
			}
		}

		try{
			String uri = getRepoServer()+"rest/keymanager";

			JSONObject object = new JSONObject();
			object.put("user", user);
			object.put("key", softwareKey);
			object.put("type", moduleVersion.getType());
			object.put("idModuleVersion", moduleVersion.getIdVersion());
			object.put("installationType", userInstall ? "USER" : "SYSTEM");
			object.put("email", analyticsStoreLoginBean.getEmail());
			object.put("password", analyticsStoreLoginBean.getPassword());

			Client client = Client.create();
			WebResource webResource = client.resource(uri);

			ClientResponse response = webResource.type("application/json")
					.post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);

			try{
				JSONObject pckObj = new JSONObject(ansServer);
				downloadUrl = getRepoServer() + pckObj.getString("url");
				fileName = pckObj.getString("fileName");
				key = pckObj.getString("key");
				name = pckObj.getString("name");
				newKey = pckObj.has("newKey") ? pckObj.getBoolean("newKey") : null;
				licenseKeyProperties = pckObj.getString("licenseKeyProperties");
				error = pckObj.getString("error");
			} catch (JSONException e){
				logger.warn(e,e);
			}

		}catch(Exception e){
			logger.warn(e,e);
		}

		if(error == null || error.isEmpty()){

			String tmp = WorkflowPrefManager.pathSysHome;
			String packagePath = tmp + "/tmp/" +fileName;

			logger.debug("packagePath " + packagePath);

			try {
				URL website = new URL(downloadUrl + "&idUser=" + analyticsStoreLoginBean.getIdUser() + "&key=" + softwareKey);
				ReadableByteChannel rbc = Channels.newChannel(website.openStream());
				FileOutputStream fos = new FileOutputStream(packagePath);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
			} catch (MalformedURLException e) {
				logger.warn(e,e);
			} catch (IOException e) {
				logger.warn(e,e);
			}

			BufferedWriter writer = null;
			try {
				File file = new File(WorkflowPrefManager.pathSystemLicence);
				String filepath = file.getAbsolutePath();
				if(file.exists()){
					file.delete();
				}
				PrintWriter printWriter = new PrintWriter(new File(filepath));
				printWriter.print(licenseKeyProperties);
				printWriter.close ();
			} catch (Exception e) {
				logger.warn(e,e);
			} finally {
				try {
					writer.close();
				} catch (Exception e) {
				}
			}

			PackageManager pckMng = new PackageManager();
			File file = new File(packagePath);
			logger.debug("file packagePath " + file.getAbsolutePath() + " - " + file.exists()+ "'");

			error = pckMng.addPackage(user, new String[]{packagePath});

			file.delete();

			if (error == null){
				MessageUseful.addInfoMessage("Package Installed.");
				installed = true;
			}else{
				disable(packagePath);
				MessageUseful.addErrorMessage("Error installing package: " + error);
			}

		}else{
			String value[] = error.split(",");
			if(value.length > 1){
				MessageUseful.addErrorMessage("Error installing package: " + getMessageResourcesWithParameter(value[0],new String[]{value[1]}));
			}else{
				MessageUseful.addErrorMessage("Error installing package: " + getMessageResources(error));
			}
		}

		//update list of packages modalPackage.xhtml
		FacesContext context = FacesContext.getCurrentInstance();
		PackageMngBean packageMngBean = (PackageMngBean) context.getApplication().evaluateExpressionGet(context, "#{packageMngBean}", PackageMngBean.class);
		if (userInstall){
			packageMngBean.calcUserPackages();
		}else{
			packageMngBean.calcSystemPackages();
		}


		String isADMPage = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("admPage");

		if(isADMPage != null && isADMPage.equals("N")){
			setShowRestartMSG("N");
		}else{
			if(error == null || error.isEmpty()){
				setShowRestartMSG("Y");
			}
		}

		return "";
	}

	public String getRepoServer(){
		String pckServer = WorkflowPrefManager.getPckManagerUri();
		if(!pckServer.endsWith("/")){
			pckServer+="/";
		}
		return pckServer;
	}

	public void disable(String packageName) {

		String softwareKey = getSoftwareKey();

		try {

			String uri = getRepoServer()+"rest/installations/disable";

			JSONObject object = new JSONObject();
			object.put("packageName", packageName);
			object.put("softwareKey", softwareKey);

			Client client = Client.create();
			WebResource webResource = client.resource(uri);

			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);

		} catch (JSONException e) {
			logger.warn(e,e);
		}

	}


	public RedSqirlModule getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(RedSqirlModule moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	public List<RedSqirlModule> getModuleVersionList(){
		return versionList;
	}

	public boolean getShowVersionNote(){
		return moduleVersion.getVersionNote() != null 
				&& !moduleVersion.getVersionNote().isEmpty();
	}

	public AnalyticsStoreLoginBean getAnalyticsStoreLoginBean() {
		return analyticsStoreLoginBean;
	}

	public void setAnalyticsStoreLoginBean(AnalyticsStoreLoginBean loginBean) {
		this.analyticsStoreLoginBean = loginBean;
	}

	public boolean isInstalled() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}

	public UserInfoBean getUserInfoBean() {
		return userInfoBean;
	}

	public void setUserInfoBean(UserInfoBean userInfoBean) {
		this.userInfoBean = userInfoBean;
	}

	public List<RedSqirlModuleVersionDependency> getRedSqirlModuleVersionDependency() {
		return redSqirlModuleVersionDependency;
	}

	public void setRedSqirlModuleVersionDependency(
			List<RedSqirlModuleVersionDependency> redSqirlModuleVersionDependency) {
		this.redSqirlModuleVersionDependency = redSqirlModuleVersionDependency;
	}

	public String getShowRestartMSG() {
		return showRestartMSG;
	}

	public void setShowRestartMSG(String showRestartMSG) {
		this.showRestartMSG = showRestartMSG;
	}

}