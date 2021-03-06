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

package com.redsqirl.workflow.test;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.idiro.Log;
import com.idiro.ProjectID;
import com.idiro.hadoop.NameNodeVar;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.action.superaction.SubWorkflowTests;
import com.redsqirl.workflow.server.action.superaction.SuperActionTests;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.datatype.HDFSTypeTests;
import com.redsqirl.workflow.utils.jdbc.GenericConfFileTests;


@RunWith(Suite.class)
@SuiteClasses({
//	ActionTests.class,
//	WorkflowTests.class,
	//FIXME CreateWorkflowTests does not work
//	CreateWorkflowTests.class,
////	ServerMainTests.class,
//	HDFSInterfaceTests.class,
//	//FIXME HiveInterfaceTests does not work
//	//HiveInterfaceTests.class,
//	//FIXME SSHInterfaceTests does not work
//	//SSHInterfaceTests.class,
//	SSHInterfaceArrayTests.class,
//	//FIXME Hive Type Tests does not work 
//	//HiveTypeTests.class, 
//	HiveTypePartitionTests.class, 
//	SourceTests.class
//	WorkflowProcessesManagerTests.class,
//	OozieManagerTests.class,
//	
//	OozieDagTests.class,
//	OrderedFieldListTests.class,
//	TreeTests.class,
//	ConvertTests.class,
//	InputInteractionTests.class,
//	AppendListInteractionTests.class,
//	ListInteractionTests.class,
//	EditorInteractionTests.class,
//	TableInteractionTests.class,
//	//FIXME Test only done for keith user...
//	HDFSTypeTests.class,
//	PackageManagerTests.class,
//	AbstractDictionaryTests.class,
//	SendEmailTests.class,
//	WorkflowInterfaceTests.class,
//	SubWorkflowTests.class,
//	SuperActionTests.class
	GenericConfFileTests.class
})
public class SetupEnvironmentTest {

	static Logger logger = null;
	static public File testDirOut = null;
	static public String pathSaveWorkflow = null;

	@BeforeClass
	public static void init() throws Exception{
		String log4jFile = SetupEnvironmentTest.class.getResource( "/log4j.properties" ).getFile();
		System.out.println(log4jFile);
		String userPrefFile = SetupEnvironmentTest.class.getResource( "/prefs" ).getFile(); 
		System.out.println(userPrefFile);
		String testProp = SetupEnvironmentTest.class.getResource( "/test.properties" ).getFile();
		System.out.println(testProp);



		ProjectID.getInstance().setName("RedSqirlWorkflowServerTest");
		ProjectID.getInstance().setVersion("0.01");
		System.out.println(ProjectID.get());

		Log log = new Log();
		log.put(log4jFile);
//		Logger.getRootLogger().setLevel(Level.DEBUG);

		WorkflowPrefManager.getInstance();
		WorkflowPrefManager.pathSysCfgPref = testProp;
		WorkflowPrefManager.pathUserCfgPref = testProp;
		logger = Logger.getLogger(SetupEnvironmentTest.class);
		File logfile = new File(log4jFile);

		if(logfile.exists()){
			BufferedReader reader = new BufferedReader(new FileReader(logfile));
			String line ="";
			while ((line = reader.readLine()) != null) {
				logger.info(line);
			}
			reader.close();
		}
		logger.debug("Log4j initialised");
		logger.info("Hive url '"+ 
						"hive_jdbc_url"+"_"+System.getProperty("user.name")+
						"': "+WorkflowPrefManager.getUserProperty(
								"hive_jdbc_url"+"_"+System.getProperty("user.name")));
		HiveInterface.setUrl(
				WorkflowPrefManager.getUserProperty(
						"hive_jdbc_url"+"_"+System.getProperty("user.name")));

		NameNodeVar.set(WorkflowPrefManager.getUserProperty(WorkflowPrefManager.sys_namenode));
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(testProp));
		} catch (Exception e) {
			logger.error("Error to load "+testProp);
			logger.error(e.getMessage());
			throw new Exception();
		}

		testDirOut = new File(new File(testProp).getParent(), prop.getProperty("outputDir"));
		logger.info("Create directory "+testDirOut.getCanonicalPath());
		testDirOut.mkdir();

		pathSaveWorkflow = prop.getProperty("path_save_workflow");

		File home = new File(testDirOut,"home_project");
		home.mkdir();
		WorkflowPrefManager.changeSysHome(home.getAbsolutePath());
		WorkflowPrefManager.createUserHome(System.getProperty("user.name"));
		WorkflowPrefManager.pathSysCfgPref = testProp;
		WorkflowPrefManager.pathUserCfgPref = testProp;
		WorkflowPrefManager.setupHome();
		logger.info(WorkflowPrefManager.pathSysHome);
		logger.info(WorkflowPrefManager.getPathuserpref());
		logger.info(WorkflowPrefManager.pathUserCfgPref);
		logger.info("starts the tests...");
	}

	@AfterClass
	public static void end(){
		logger.info("Clean testing environment...");
		HDFSInterface hdfsInt = null;
		try {
			hdfsInt = new HDFSInterface();
			for(int i = 0; i < 4; ++i){
				hdfsInt.delete(TestUtils.getPath(i));
			}
		}catch (Exception e) {
			logger.error("something went wrong : " + e.getMessage());

		}
//		HiveInterface hiveInt = null;
//		try {
//			hiveInt = new HiveInterface();
//			for(int i = 0; i < 4; ++i){
//				hiveInt.delete(TestUtils.getTablePath(i));
//			}
//		}catch (Exception e) {
//			logger.error("something went wrong : " + e.getMessage());
//
//		}

		WorkflowPrefManager.resetSys();
		WorkflowPrefManager.resetUser();
		logger.info(WorkflowPrefManager.pathSysHome);
	}
}
