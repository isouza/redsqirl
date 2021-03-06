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

package com.redsqirl.workflow.server.interfaces;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.redsqirl.workflow.server.enumeration.SavingState;

/**
 * Data Flow interface.
 * 
 * @author etienne
 * 
 */
public interface DataFlow extends Remote, Cloneable{
	
	/**
	 * Get the element manager of the workflow
	 * @return ElementManager
	 * @throws RemoteException
	 */
	public ElementManager getElementManager() throws RemoteException;
	
	/**
	 * Check if a workflow is correct or not. Returns a string with a
	 * description of the error if it is not correct.
	 * 
	 * @return the error.
	 */
	public String check() throws RemoteException;

	/**
	 * Run the workflow
	 * 
	 * @return error message
	 * @throws Exception
	 *             an exception with a message if something goes wrong
	 */
	public String run() throws RemoteException, Exception;

	/**
	 * Run the workflow
	 * 
	 * @param dataFlowElement list of element to run
	 * @return error message
	 * @throws Exception
	 *             an exception with a message if something goes wrong
	 */
	public String run(List<String> dataFlowElement) throws RemoteException;

	/**
	 * List the elements to run, removing the actions that already produced the data
	 * @param dataFlowElements
	 * @return List the elements to run
	 * @throws Exception
	 */
	public List<RunnableElement> subsetToRun(List<String> dataFlowElements) throws Exception;
	
	/**
	 * Check if workflow is running
	 * 
	 * @return <code>true</code> if the workflow is currently processed (for
	 *         Oozie running OR suspended) else <code>false</code>
	 */
	public boolean isrunning() throws RemoteException;

	public String getRunningStatus(String componentId) throws RemoteException;
	
	/**
	 * Remove temporary and buffered data for the entire project.
	 * 
	 * @return Error message
	 * @throws RemoteException
	 */
	public String cleanProject() throws RemoteException;

	/**
	 * Regenerate path and copy or move the existing data
	 * 
	 * @param copy
	 *            null only set the path, true to copy, false to move
	 * @return Error message
	 * @throws RemoteException
	 */
	public String regeneratePaths(Boolean copy) throws RemoteException;

	/**
	 * Save a workflow. A workflow is a zip file containing two files: - an xml
	 * representing the workflow - a file containing the values for each action
	 * 
	 * @param file
	 *            the file path
	 * @return null if OK, or a description of the error.
	 */
	public String save(String file) throws RemoteException;

	/**
	 * Close a workflow, clean temporary data if necessary.
	 * 
	 * @throws RemoteException
	 */
	public void close() throws RemoteException;

	/**
	 * Do an automatic backup of the workflow.
	 * @return string
	 */
	public String backup() throws RemoteException;

	/**
	 * Return true if the dataflow has been loaded or has been saved.
	 * 
	 * @return <code>true</code> if the dataflow has been saved else
	 *         <code>false</code>
	 * @throws RemoteException
	 */
	public boolean isSaved() throws RemoteException;

	/**
	 * Reads a workflow
	 * 
	 * A workflow is a zip file containing two files: - an xml representing the
	 * workflow - a file containing the values for each action
	 * 
	 * @param file
	 *            the file path to read from
	 * @return null if OK, or a description of the error.
	 */
	public String read(String file) throws RemoteException;
	
	/**
	 * Reads a workflow from a local file.
	 */
	public String readFromLocal(File f) throws RemoteException;

	/**
	 * Do sort of the workflow.
	 * 
	 * If the sort is successful, it is a DAG
	 * 
	 * @return null if OK, or a description of the error.
	 */
	public String topoligicalSort() throws RemoteException;

	/**
	 * Generate a unique id that can be used for a new Element
	 * @return A new ID.
	 * @throws RemoteException
	 */
	public String generateNewId() throws RemoteException;
	
	/**
	 * Add a WorkflowAction in the Workflow. The element is at the end of the
	 * workingWA list.  
	 * @see com.redsqirl.workflow.server.interfaces.DataFlowElement#getName()
	 * 
	 * @param elementName
	 *            the name of the action
	 * @return The new element id
	 * @throws Exception
	 *             an exception with a message if something goes wrong
	 */
	public String addElement(String elementName) throws RemoteException,
			Exception;
	
	/**
	 * Add a DataFlowElement to the workflow
	 * @param dfe
	 * @throws RemoteException
	 */
	public void addElement(DataFlowElement dfe) throws RemoteException;
	
	/**
	 * Change the id of an element
	 * 
	 * @param oldId
	 *            The old id
	 * @param newId
	 *            The new id
	 * @return The error if any or null
	 * @throws RemoteException
	 */
	public String changeElementId(String oldId, String newId)
			throws RemoteException;

	public String removeElement(String componentId) throws RemoteException,
			Exception;

	
	/**
	 * Create a SubDataFlow from workflow elements.
	 * @param componentIds The components to aggregate
	 * @param subworkflowName The name of the new Super Action
	 * @param subworkflowComment A comment associated with the new Super Action
	 * @param inputs the input names with the component id and output name (those components are not in the componentIds list)
	 * @param outputs the output names with the component id and output name (those components are in the componentIds list)
	 * @return The new SubDataFlow
	 * @throws Exception with an error message
	 */
	public SubDataFlow createSA(
			List<String> componentIds, 
			String subworkflowName,
			String subworkflowComment,
			Map<String,Entry<String,String>> inputs, 
			Map<String,Entry<String,String>> outputs) throws RemoteException,Exception;
	
	
	/**
	 * Rename a Super Element so that it point to a different Super Action.
	 * The new Super Action should have the same entry than the old one.  
	 * @param oldName
	 * @param newName
	 * @throws RemoteException
	 */
	public void renameSA(String oldName, String newName) throws RemoteException;
	
	/**
	 * Aggregate the Elements in one existing SuperAction
	 * @param componentIds The components to aggregate
	 * @param subworkflowName The name of the new Super Action
	 * @param inputs the input names with the component id and output name (those components are not in the componentIds list)
	 * @param outputs the output names with the component id and output name (those components are in the componentIds list)
	 * @return The error message if any or null
	 * @throws RemoteException
	 */
	public String aggregateElements(
			List<String> componentIds, 
			String subworkflowName,
			Map<String,Entry<String,String>> inputs, 
			Map<String,Entry<String,String>> outputs) throws RemoteException;
	
	/**
	 * Expand the Element in the current canvas
	 * @param componentId The components to expand
	 * @return The error message if any or null
	 * @throws RemoteException
	 */
	public String expand(String componentId) throws RemoteException;

	/**
	 * Replace string in the interaction of the given elements 
	 * @param componentIds
	 * @param oldStr String to replace
	 * @param newStr The replacement
	 * @param regex True if oldStr is a regular expression
	 * @throws RemoteException
	 */

	void replaceInAllElements(List<String> componentIds, String oldStr, String newStr, boolean regex)
			throws RemoteException;
	
	/**
	 * Get the WorkflowAction corresponding to the componentId.
	 * @see com.redsqirl.workflow.server.interfaces.DataFlowElement#getComponentId
	 * 
	 * @param componentId
	 *            The id of the action.
	 * @return a WorkflowAction object or null
	 */
	public DataFlowElement getElement(String componentId)
			throws RemoteException;

	/**
	 * Remove a link. If the link creation imply a topological error it cancel
	 * it. To understand the nomenclature: out --> in
	 * 
	 * @param inName
	 *            relation between the edge and the output vertex
	 * @param componentIdIn
	 *            the output vertex id
	 * @param outName
	 *            relation between the edge and the input vertex
	 * @param componentIdOut
	 *            the input vertex id
	 * @return null if OK, or a description of the error.
	 */
	public String removeLink(String outName, String componentIdOut,
			String inName, String componentIdIn) throws RemoteException;

	/**
	 * Add a link. If the link creation imply a topological error it cancel it.
	 * To understand the nomenclature: out --> in
	 * 
	 * @param inName
	 *            relation between the edge and the output vertex
	 * @param componentIdIn
	 *            the output vertex id
	 * @param outName
	 *            relation between the edge and the input vertex
	 * @param componentIdOut
	 *            the input vertex id
	 * @return null if OK, or a description of the error.
	 */
	public String addLink(String outName, String componentIdOut, String inName,
			String componentIdIn) throws RemoteException;

	/**
	 * Remove a link. To understand the nomenclature: out --> in
	 * 
	 * @param inName
	 *            relation between the edge and the output vertex
	 * @param componentIdIn
	 *            the output vertex id
	 * @param outName
	 *            relation between the edge and the input vertex
	 * @param componentIdOut
	 *            the input vertex id
	 * @param force
	 *            if false cancel the action if it implies a topological error
	 * @return null if OK, or a description of the error.
	 */
	public String removeLink(String outName, String componentIdOut,
			String inName, String componentIdIn, boolean force)
			throws RemoteException;

	/**
	 * Add a link. If the link creation imply a topological error it cancel it.
	 * To understand the nomenclature: out --> in
	 * 
	 * @param inName
	 *            relation between the edge and the output vertex
	 * @param componentIdIn
	 *            the output vertex id
	 * @param outName
	 *            relation between the edge and the input vertex
	 * @param componentIdOut
	 *            the input vertex id
	 * @param force
	 *            if false cancel the action if it implies a topological error
	 * @return null if OK, or a description of the error.
	 */
	public String addLink(String outName, String componentIdOut, String inName,
			String componentIdIn, boolean force) throws RemoteException;

	/**
	 * Get all the WorkflowAction available in the jars file.
	 * 
	 * To find the jars, the method use
	 * 
	 * @see com.redsqirl.workflow.server.WorkflowPrefManager#getNonAbstractClassesFromSuperClass(String)
	 * @see com.redsqirl.workflow.server.interfaces.DataFlowElement#getName()
	 *      .
	 * 
	 * @return the dictionary: key action name ; value the canonical class name.
	 * @throws Exception
	 *             if one action cannot be load
	 */
	public Map<String, String> getAllWANameWithClassName()
			throws RemoteException, Exception;

	/**
	 * Get all the WorkflowAction available in the jars file.
	 * 
	 * @see com.redsqirl.workflow.server.WorkflowPrefManager#getNonAbstractClassesFromSuperClass(String)
	 * @see com.redsqirl.workflow.server.interfaces.DataFlowElement#getName() 
	 * 
	 * @return Array of all the action with name, image path, help path.
	 * @throws Exception
	 *             if one action cannot be load
	 */
	public List<String[]> getAllWA() throws RemoteException;

	/**
	 * Get the list of Workflow elements
	 * 
	 * @return the workingWA
	 */
	public List<DataFlowElement> getElement() throws RemoteException;

	/**
	 * Get the last elment of workingWA
	 * 
	 * @return the last element of workingWA.
	 */
	public DataFlowElement getLastElement() throws RemoteException;

	/**
	 * 
	 * @return the menuWA
	 */
	public Map<String, List<String[]>> getMenuWA() throws RemoteException;


	/**
	 * Get the list of compnent Ids that are an the Workflow
	 * 
	 * @return list of the component Ids
	 * @throws RemoteException
	 */
	public List<String> getComponentIds() throws RemoteException;

	/**
	 * Get the workflow name
	 * 
	 * @return Name of the Workflow
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException;

	/**
	 * Set the Workflow name
	 * 
	 * @param name
	 * @throws RemoteException
	 */
	public void setName(String name) throws RemoteException;

	/**
	 * Get the workflow comment
	 * 
	 * @return Comment of the Workflow
	 * @throws RemoteException
	 */
	public String getComment() throws RemoteException;

	/**
	 * Set the Workflow comment
	 * 
	 * @param comment
	 * @throws RemoteException
	 */
	public void setComment(String comment) throws RemoteException;
	
	/**
	 * Get the oozie job id of the job currently running or previously run
	 * 
	 * @return Job Id on oozie
	 * @throws RemoteException
	 */
	public String getOozieJobId() throws RemoteException;

	/**
	 * Set the oozie job id
	 * 
	 * @param oozieJobId
	 * @throws RemoteException
	 */
	public void setOozieJobId(String oozieJobId) throws RemoteException;

	/**
	 * Get the Number of element run by oozie the last time
	 */
	public int getNbOozieRunningActions() throws RemoteException;
	
	/**
	 * Check if the output of an element is a valid input of an other element
	 * 
	 * @param outName
	 * @param componentIdOut
	 * @param inName
	 * @param componentIdIn
	 * @return True if a link can be created
	 * @throws RemoteException
	 */
	public boolean check(String outName, String componentIdOut, String inName,
			String componentIdIn) throws RemoteException;

	public String backupAllWorkflowsBeforeClose() throws RemoteException;
	
	public String getPath() throws RemoteException;

	public void setPath(String path) throws RemoteException;

	Set<String> getSADependencies() throws RemoteException;

	public String cleanSelectedAction(List<String> ids) throws RemoteException;

	public void setOutputType(List<String> elements, SavingState buffered) throws RemoteException;

	
}