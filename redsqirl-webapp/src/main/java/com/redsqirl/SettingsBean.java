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

package com.redsqirl;


import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import com.redsqirl.dynamictable.SettingsControl;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.settings.Setting;
import com.redsqirl.workflow.settings.SettingInt;
import com.redsqirl.workflow.settings.SettingMenuInt;

/**
 * Class to show and modify user and system settings.
 * @author etienne
 *
 */
public class SettingsBean extends SettingsBeanAbs implements Serializable  {

	private static final long serialVersionUID = -8458743488606765997L;

	private static Logger logger = Logger.getLogger(SettingsBean.class);
	protected boolean canEdit;
	

	public void storeNewSettings(){
		logger.info("storeNewSettings");
		String error = null;
		if(isAdmin()){
			error = storeSysSettings();
		}
		if(error == null){
			error = storeUserSettings();
		}
		
		displayErrorMessage(error, "NEWSETTINGS");

	}
	

	protected String storeUserSettings(){
		logger.info("store user properties");
		String error = null;
		try {
			Properties userProp = getPrefs().getUserProperties();
			StringBuffer newPath = new StringBuffer();
			for (String value : getPath()) {
				newPath.append("."+value);
			}
			String path = newPath.substring(1);
			userProp = updateProperty(userProp,path,s.getProperties(), Setting.Scope.USER);
			getPrefs().storeUserProperties(userProp);
		} catch (IOException e) {
			error = e.getMessage();
		}
		return error;
	}

	public void defaultSettings() {
		logger.info("defaultSettings");

		try {

			getPrefs().readDefaultSettingMenu();
			curMap = getPrefs().getDefaultSettingMenu();

			if(path == null){
				path = new ArrayList<String>();
			}

			setPathPosition(WorkflowPrefManager.core_settings);
			mountPath(WorkflowPrefManager.core_settings);

			canEditPackageSettings();
			
		} catch (RemoteException e) {
			logger.error(e,e);
		}
	}
	
	public void refreshPath() throws RemoteException{
		logger.info("refresh path");
		s = mountPackageSettings(path);

		if(s.isTemplate()){
			setTemplate("Y");
		}else{
			setTemplate("N");
		}

		listSubMenu = new ArrayList<SettingsControl>();
		for (Entry<String, SettingMenuInt> settingsMenu : s.getMenu().entrySet()) {
			SettingsControl sc = new SettingsControl();

			if(s.isTemplate()){
				sc.setTemplate("Y");
			}else{
				sc.setTemplate("N");
			}

			String n = null;
			if(settingsMenu.getKey().contains(".")){
				n = settingsMenu.getKey().substring(settingsMenu.getKey().lastIndexOf(".")+1, settingsMenu.getKey().length());
			}else{
				n = settingsMenu.getKey();
			}

			sc.setName(n);
			listSubMenu.add(sc);
		}

		listSetting = new ArrayList<String>();

		Map<String,SettingInt> props = s.getProperties();
		for (Entry<String, SettingInt> settings : props.entrySet()) {
			listSetting.add(settings.getKey());
			SettingInt setting = settings.getValue();
			
			if(!SettingInt.Scope.USER.equals(setting.getScope())){
				String propValue = s.getSysValue(settings.getKey());
				if(propValue != null){
					setting.setSysValue(propValue);
					setting.setExistSysProperty(true);
				}else{
					setting.setExistSysProperty(false);
				}

			}
			
			if(!SettingInt.Scope.SYSTEM.equals(setting.getScope())){
				String propValue = s.getUserValue(settings.getKey());
				logger.info(settings.getKey()+": "+propValue);
				if(propValue != null){
					setting.setUserValue(propValue);
					setting.setExistUserProperty(true);
				}else{
					setting.setExistUserProperty(false);
				}

			}

		}
		//check if is admin
		canEditPackageSettings();

	}
	
	public void readCurMap() throws RemoteException{
		getPrefs().readDefaultSettingMenu();
		curMap = getPrefs().getDefaultSettingMenu();
	}
	

	public void addNewTemplate() throws RemoteException{

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");
		String error = null;

		StringBuffer newPath = new StringBuffer();
		for (String value : getPath()) {
			newPath.append(value+".");
		}
		newPath.append(name);
		String path = newPath.toString();
		
		
		Map<String,SettingInt> templateSetting = new LinkedHashMap<String,SettingInt>();
		Map<String,String[]> langMsg = new LinkedHashMap<String,String[]>();
		for (Entry<String, SettingInt> setting : s.getProperties().entrySet()) {
			if(setting.getValue().getScope().equals(SettingInt.Scope.SYSTEM) ){
				if(!isAdmin()){
					error = "You are not allowed to create a template for this item, you required to be administrator.";
				}else{
					templateSetting.put(setting.getKey(), new Setting(setting.getValue()));
					templateSetting.get(setting.getKey()).setSysValue(setting.getValue().getDefaultValue());
				}
			}else{
				templateSetting.put(setting.getKey(), new Setting(setting.getValue()));
				templateSetting.get(setting.getKey()).setUserValue(setting.getValue().getDefaultValue());
			}
			String[] msg = {setting.getValue().getDescription(),setting.getValue().getLabel()};
			langMsg.put(path+"."+setting.getKey(),msg);
		}
		
		if(error == null){
			try{
				if(!templateSetting.isEmpty() && isAdmin()){
					Properties sysProp = WorkflowPrefManager.getSysProperties();
					sysProp = updateProperty(sysProp,path,templateSetting, Setting.Scope.SYSTEM);
					WorkflowPrefManager.storeSysProperties(sysProp);
				}
				if(!templateSetting.isEmpty()){
					Properties userProp = getPrefs().getUserProperties();
					userProp = updateProperty(userProp,path,templateSetting, Setting.Scope.USER);
					getPrefs().storeUserProperties(userProp);
				}
				storeNewSettingsLang(langMsg);
			}catch(Exception e){
				logger.error(e,e);
				error = "Fail to write settings";
			}
			readCurMap();
			refreshPath();
		}
		
		displayErrorMessage("ADDTEMPLATE", error);

	}

	public void removeTemplate() throws RemoteException{
		String error = null;
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		StringBuffer pathToDelete = new StringBuffer();
		for (String value : getPath()) {
			pathToDelete.append(value+".");
		}
		pathToDelete.append(name);
		String path = pathToDelete.toString();
		Properties sysProp = WorkflowPrefManager.getSysProperties();
		Properties userProp = getPrefs().getUserProperties();
		
		for (Entry<String, SettingInt> setting : s.getProperties().entrySet()) {
			String key = path +"."+setting.getKey();
			if(setting.getValue().getScope().equals(SettingInt.Scope.SYSTEM) ){
				if(!isAdmin()){
					error = "You are not allowed to create a template for this item, you required to be administrator.";
				}
			}
			sysProp.remove(key);
			userProp.remove(key);
		}
		
		if(error == null){
			try{
				if(isAdmin()){
					WorkflowPrefManager.storeSysProperties(sysProp);
				}
				getPrefs().storeUserProperties(userProp);
				readCurMap();
				refreshPath();
			}catch(Exception e){
				logger.error(e,e);
				error = e.getMessage();
			}
		}
		
		displayErrorMessage("REMOVETEMPLATE", error);
		
	}
	
	private void canEditPackageSettings() throws RemoteException{
		if(isAdmin()){
			setCanEdit(true);
		}else{
			setCanEdit(false);
		}
	}

	public boolean isCanEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}

}