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

package com.redsqirl.utils;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.utils.TreeNonUnique;
import com.redsqirl.workflow.server.enumeration.FieldType;

public class TreeTests {

	private Logger logger = Logger.getLogger(getClass());
	
	@Test
	public void TreeTestBasic() throws RemoteException{
		
		Tree<String> input = new TreeNonUnique<String>("table");
		Tree<String> columns = new TreeNonUnique<String>("columns");
		input.add(columns);

		String table_op_title = "Operations";
		String table_field_title= "Field";
		String table_type_title= "Type";
		// operation
		columns.add("column").add("title").add(table_op_title);

		// Field name
		Tree<String> newFieldName = new TreeNonUnique<String>("column");
		columns.add(newFieldName);
		newFieldName.add("title").add(table_field_title);

		Tree<String> constraintField = new TreeNonUnique<String>("constraint");
		newFieldName.add(constraintField);
		constraintField.add("count").add("1");

		// Type
		Tree<String> newType = new TreeNonUnique<String>("column");
		columns.add(newType);
		newType.add("title").add(table_type_title);

		Tree<String> constraintType = new TreeNonUnique<String>("constraint");
		newType.add(constraintType);

		Tree<String> valsType = new TreeNonUnique<String>("values");
		constraintType.add(valsType);

		valsType.add("value").add(FieldType.BOOLEAN.name());
		valsType.add("value").add(FieldType.INT.name());
		valsType.add("value").add(FieldType.DOUBLE.name());
		valsType.add("value").add(FieldType.FLOAT.name());
		valsType.add("value").add("BIGINT");
		
		assertTrue("Input head incorrect",input.getHead().equals("table"));
		assertTrue("Input child incorrect",input.getFirstChild().getHead().equals("columns"));
		assertTrue("Input size",input.getSubTreeList().size() == 1);
		assertTrue("Input size 2",input.getFirstChild().getSubTreeList().size() == 3); 
		logger.info(input.getFirstChild().getHead());
	}
	
	@Test
	
	public void TreeDepth() throws RemoteException{
		
		Tree<String> tree = new TreeNonUnique<String>("test"); 
		tree.add("testl1");
		assertTrue("Search incorrect.",tree.getFirstChild("test2") == null);
		
	}

}
