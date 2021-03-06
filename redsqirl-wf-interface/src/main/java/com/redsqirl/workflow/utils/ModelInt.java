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

package com.redsqirl.workflow.utils;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.redsqirl.workflow.server.interfaces.SubDataFlow;

/**
 * Interface of a model: a set of subworkflows.
 * 
 * A model has a version, an mage, can include private subworkflows private, can be editable, can depend on each other.
 * A model
 * @author etienne
 *
 */
public interface ModelInt extends Remote{

	/**
	 * The model folder. The name of the folder is the model name.
	 * @return File
	 */
	File getFile() throws RemoteException;
	
	/**
	 * Get the model name.
	 * @return String
	 */
	String getName() throws RemoteException;
	
	/**
	 * True if the model is editable.
	 * @return boolean
	 */
	boolean isEditable() throws RemoteException;
	
	/**
	 * Set if a model is editable or not.
	 * @param editable
	 */
	void setEditable(boolean editable) throws RemoteException;
	
	/**
	 * Get the version of the model.
	 * @return string
	 */
	String getVersion() throws RemoteException;
	
	/**
	 * Set the version of the model.
	 * @param version
	 */
	void setVersion(String version) throws RemoteException;
	
	/**
	 * Reset the workflow image.
	 */
	void resetImage() throws RemoteException;
	
	/**
	 * Seth a new image for representing all the subworkflows of the model.
	 * @param imageFile
	 */
	void setImage(File imageFile) throws RemoteException;
	
	/**
	 * Add a subworkflow to the private list.
	 * A private subworkflow can't be called from outside of the model.
	 * @param subworkflowName
	 */
	void addToPrivate(String subworkflowName) throws RemoteException;
	
	/**
	 * Remove a subworkflow from the private list.
	 * @param subworkflowName
	 */
	void removeFromPrivate(String subworkflowName) throws RemoteException;
	
	/**
	 * Get all the sub workflow names of the model.
	 * @return Set<String>
	 */
	Set<String> getSubWorkflowNames() throws RemoteException;
	
	/**
	 * Get the name of the sub workflow names.
	 * @return Set<String>
	 */
	Set<String> getPublicSubWorkflowNames() throws RemoteException;
	
	/**
	 * Get the full name of the public workflows. 
	 * The full name is composed of the model followed by the subworkflow names. 
	 * @return Set<String>
	 */
	Set<String> getPublicFullNames() throws RemoteException;
	
	/**
	 * Get the full name of the subworkflows. 
	 * The full name is composed of the model followed by the subworkflow names. 
	 * @return Set<String>
	 */
	Set<String> getSubWorkflowFullNames() throws RemoteException;
	
	/**
	 * Get the dependencies of the entire model.  
	 * @return Set<String>
	 */
	Set<String> getAllDependencies() throws RemoteException;
	
	/**
	 * Add dependencies required to run a subworkflow.
	 * 
	 * @param subworkflowName
	 * @param dependencies
	 */
	void addSubWorkflowDependencies(String subworkflowName,Set<String> dependencies) throws RemoteException;
	
	/**
	 * Remove dependencies required to run a subworkflow.
	 * 
	 * @param subworkflowName
	 * @param dependencies
	 */
	void removeSubWorkflowDependencies(String subworkflowName,Set<String> dependencies) throws RemoteException;
	
	/**
	 * Get the dependencies required to run a given subworkflow.
	 * @param subworkflowName
	 * @return Set<String>
	 */
	Set<String> getSubWorkflowDependencies(String subworkflowName) throws RemoteException;
	
	/**
	 * Get the dependencies related to each subworkflow.
	 * @return Map<String,Set<String>>
	 */
	Map<String,Set<String>> getDependenciesPerSubWorkflows() throws RemoteException;
	
	/**
	 * Install SubDataFlow with the given privileges.
	 * @param toInstall
	 * @param privilege
	 * @return error
	 * @throws RemoteException
	 */
	public String install(SubDataFlow toInstall, Boolean privilege) throws RemoteException;

	public String installHelp(SubDataFlow toInstall) throws RemoteException;
	
	
	public String importModel(File modelZipFile) throws RemoteException;
	
	/**
	 * Delete a subworkflow
	 * @param name
	 */
	void delete(String name) throws RemoteException;

	Set<String> getSubWorkflowFullNames(Collection<String> subworkflowNames) throws RemoteException;

	boolean isSystem() throws RemoteException;

	void removeAllDependencies() throws RemoteException;

	void addSubWorkflowDependencyLines(Set<String> dependencyLine) throws RemoteException;

	String getUser() throws RemoteException;

	Set<String> getSubWorkflowFullNameDependentOn(Set<String> subworkflowFullNames) throws RemoteException;

	String getFullName(String saName) throws RemoteException;

	String createModelDir() throws RemoteException;
	
	public String readLicense(String userName) throws RemoteException;

	String getComment() throws RemoteException;
	
	void setComment(String comment) throws RemoteException;

	File getTomcatImage() throws RemoteException;

	String isLicenseValid(String userName) throws RemoteException;
}
