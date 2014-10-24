package com.redsqirl.workflow.server.interfaces;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Map;

/**
 * A SuperElement is an element that runs a SubDataFlow Oozie Action.
 * 
 * @author etienne
 *
 */
public interface SuperElement extends DataFlowElement{

	/**
	 * @return the tmpOutput
	 */
	Map<LinkedList<String>, DFEOutput> getTmpOutput() throws RemoteException;

	/**
	 * @param tmpOutput the tmpOutput to set
	 */
	void setTmpOutput(Map<LinkedList<String>, DFEOutput> tmpOutput) throws RemoteException;
	
	public void updateOozieSubWorkflowAction() throws RemoteException;
}