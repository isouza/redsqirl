package com.redsqirl.workflow.server;


import java.rmi.RemoteException;

import com.redsqirl.workflow.server.enumeration.DisplayType;
/**
 * Implent a browser interaction 
 * @author keith
 *
 */
public class BrowserInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4244649045214883613L;
	/**
	 * Constructor for the browser
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public BrowserInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, DisplayType.browser, column, placeInColumn);
	}
	/**
	 * Constructor for the browser
	 * @param id
	 * @param name
	 * @param legend
	 * @param texttip
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public BrowserInteraction(String id, String name, String legend,
			String texttip, int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, texttip, DisplayType.browser, column, placeInColumn);
	}

}