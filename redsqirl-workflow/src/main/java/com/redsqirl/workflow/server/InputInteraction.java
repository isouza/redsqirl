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

package com.redsqirl.workflow.server;


import java.rmi.RemoteException;
import java.util.regex.Pattern;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.enumeration.DisplayType;
/**
 * Interaction for character input 
 * @author keith
 *
 */
public class InputInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7192633417256406554L;
	/**
	 * Constructor with necessary parameters
	 * @param id 
	 * @param name
	 * @param legend the description of the interaction
	 * @param column which column to place the interaction
	 * @param placeInColumn place in the column for interaction to reside
	 * @throws RemoteException
	 */
	public InputInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, DisplayType.input, column, placeInColumn);
		init();
	}
	/**
	 * Constructor with necessary parameters
	 * @param id 
	 * @param name
	 * @param legend the description of the interaction
	 * @param texttip the text tip
	 * @param column which column to place the interaction
	 * @param placeInColumn place in the column for interaction to reside
	 * @throws RemoteException
	 */
	public InputInteraction(String id, String name, String legend,
			String texttip,int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, texttip,DisplayType.input, column, placeInColumn);
		init();
	}
	/** 
	 * Init the interactions tree
	 * @throws RemoteException
	 */
	protected void init() throws RemoteException{
		Tree<String> input = null;
		if(tree.isEmpty()){
			input = tree.add("input");
			input.add("output");
			input.add("regex");
		}
	}
	
	protected void reInitAfterError() throws RemoteException{
		if(tree.getFirstChild("input") == null){
			tree.add("input");
		}
		if(tree.getFirstChild("input").getFirstChild("regex") == null){
			tree.getFirstChild("input").add("regex");
		}
		if(tree.getFirstChild("input").getFirstChild("output") == null){
			tree.getFirstChild("input").add("output");
		}
	}
	/**
	 * Get the value currently stored in the interaction
	 * @return value stored in interaction
	 * @throws RemoteException
	 */
	public String getValue() throws RemoteException{
		String ans = null;
		try{
			if(getTree().getFirstChild("input").getFirstChild("output").getFirstChild() != null){
				ans = getTree().getFirstChild("input").getFirstChild("output").getFirstChild().getHead();
			}
		}catch(Exception e){
			logger.warn(getId()+": Tree structure incorrect",e);
			reInitAfterError();
			ans = "";
		}
		return ans;
	}
	/**
	 * Get the regex that checks the input for errors
	 * @return regex string
	 * @throws RemoteException
	 */
	public String getRegex() throws RemoteException{
		String ans = null;
		try{
			if(getTree().getFirstChild("input").getFirstChild("regex").getFirstChild() != null ){
				ans = getTree().getFirstChild("input").getFirstChild("regex").getFirstChild().getHead();
			}
		}catch(Exception e){
			logger.warn(getId()+": Tree structure incorrect",e);
			reInitAfterError();
		}
		return ans;
	}
	
	/**
	 * Replace the values in regex and value
	 */
	@Override
	public void replaceOutputInTree(String oldName, String newName, boolean regex)
			throws RemoteException {
		String val = getValue();
		if(val != null && !val.isEmpty()){
			if(regex){
				getTree().getFirstChild("input").getFirstChild("output")
				.getFirstChild().setHead(val.replaceAll(oldName, newName));
			}else{
				getTree().getFirstChild("input").getFirstChild("output")
					.getFirstChild().setHead(val.replaceAll(Pattern.quote(oldName), newName));
			}
		}
	}
	/**
	 * Set the value for the interaction
	 * @param value
	 * @throws RemoteException
	 */
	public void setValue(String value) throws RemoteException{
		String regex = getRegex();
		if(value == null && (regex == null || regex.isEmpty())){
			getTree().getFirstChild("input").getFirstChild("output").removeAllChildren();
		}else if( regex == null || regex.isEmpty() || (value != null && value.matches(regex))){
			getTree().getFirstChild("input").getFirstChild("output").removeAllChildren();
			getTree().getFirstChild("input").getFirstChild("output").add(value);
		}
	}
	/**
	 * Set the interactions regex for checking the input
	 * @param regex
	 * @throws RemoteException
	 */
	public void setRegex(String regex) throws RemoteException{
		try{
			getTree().getFirstChild("input").getFirstChild("regex").removeAllChildren();
		}catch(Exception e){
			logger.warn(getId()+": Tree structure incorrect",e);
			reInitAfterError();
		}
		getTree().getFirstChild("input").getFirstChild("regex").add(regex);
	}

}
