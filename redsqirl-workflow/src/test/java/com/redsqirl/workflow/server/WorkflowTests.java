package com.redsqirl.workflow.server;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.test.TestUtils;

public class WorkflowTests {

	protected Logger logger = Logger.getLogger(getClass());

	@Test
	public void testLink(){
		TestUtils.logTestTitle("WorkflowTests#testLink");
		
		BasicWorkflowTest bwt;
		try {
			bwt = new BasicWorkflowTest(new Workflow(),new Workflow());
			bwt.linkCreationDeletion();
		} catch (RemoteException e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
		logger.debug("WorkflowTests#testLink successful");
	}
	
	@Test
	public void testReadSave(){
		TestUtils.logTestTitle("WorkflowTests#testReadSave");
		BasicWorkflowTest bwt;
		try {
			bwt = new BasicWorkflowTest(new Workflow(),new Workflow());
			bwt.readSaveElementDeletion();
		} catch (RemoteException e) {
			logger.error(e.getMessage());
			assertTrue(false);
		}
		logger.debug("WorkflowTests#testReadSave successful");
	}
	
}