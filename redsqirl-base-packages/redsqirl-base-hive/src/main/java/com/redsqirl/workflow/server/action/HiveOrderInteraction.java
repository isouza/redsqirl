package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.AppendListInteraction;

public class HiveOrderInteraction extends AppendListInteraction{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7913845575238427401L;
	/**
	 * Action that the query belongs to
	 */
	private HiveElement el;

	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public HiveOrderInteraction(String id, String name, String legend,
			int column, int placeInColumn, HiveElement el) throws RemoteException {
		super(id, name, legend, column, placeInColumn, true);
		this.el = el;
	}
	
	/**
	 * Update the interaction 
	 * @throws RemoteException
	 */
	public void update() throws RemoteException{
		FieldList fields = el.getNewFields();
		setPossibleValues(fields.getFieldNames());
		
	}
	/**
	 * Get the query piece for the interaction
	 * @param relation
	 * @return query
	 * @throws RemoteException
	 */
	public String getQueryPiece() throws RemoteException{
		String query="";
		if(check() == null && !getValues().isEmpty()){
			query = "CLUSTER BY ";
			for (int i = 0; i < getValues().size(); ++i){
				
				query += getValues().get(i);
				
				if (i < getValues().size() - 1){
					query += ",";
				}
			}
		}
		return query;
	}


}
