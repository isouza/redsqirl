package idiro.workflow.server.connect.interfaces;

import static org.junit.Assert.assertTrue;
import idiro.workflow.server.connect.SSHInterface;
import idiro.workflow.test.TestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * To Run these test, we suppose that a RSA key
 * is set up with localhost
 * @author etienne
 *
 */
public class SSHInterfaceTests {
	Logger logger = Logger.getLogger(getClass());


	@Test
	public void basic(){
		TestUtils.logTestTitle("HDFSInterfaceTests#basic");
		try{
			SSHInterface hInt = new SSHInterface("localhost",22);
			
			logger.debug("We are in: "+hInt.getPath());
			
			String new_path0 = hInt.getPath()+"/test_idm_0";
			assertTrue("create "+new_path0,
					hInt.create(new_path0, new HashMap<String,String>()) == null
					);
			
			String new_path1 = hInt.getPath()+"/test_idm_1"; 
			assertTrue("create "+new_path1,
					hInt.create(new_path1, new HashMap<String,String>()) == null
					);
			
			String new_path2 = hInt.getPath()+"/test_idm_2";
			assertTrue("move to "+new_path2,
					hInt.move(new_path1, new_path2) == null);
			
			String new_path3 = new_path2+"/test_idm_3";
			assertTrue("create "+new_path3,
					hInt.create(new_path3, new HashMap<String,String>()) == null
					);
			
			assertTrue("Fail to go to "+new_path2, hInt.goTo(new_path0));
			assertTrue("getPath: "+hInt.getPath(),
					hInt.getPath().equals(new_path0));
			
			assertTrue("Fail to go to "+new_path2, hInt.goTo(new_path2));
			
			assertTrue("getPath: "+hInt.getPath(),
					hInt.getPath().equals(new_path2));
			
			Set<String> child = hInt.getChildrenProperties().keySet();
			assertTrue("number of children should be one instead of "+child.toString(),child.size()==1);
			assertTrue("Child is "+new_path3+" instead of "+child.toString(),child.contains(new_path3));
			
			hInt.goPrevious();
			assertTrue("getPath: "+hInt.getPath(),
					hInt.getPath().equals(new_path0));
			
			hInt.goNext();
			assertTrue("getPath: "+hInt.getPath(),
					hInt.getPath().equals(new_path2));
			
			assertTrue("delete "+new_path0,
					hInt.delete(new_path0) == null);
			assertTrue("delete "+new_path2,
					hInt.delete(new_path2) == null);
			
			hInt.close();
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
	
	
	@Test
	public void check_save(){
		TestUtils.logTestTitle("HDFSInterfaceTests#save");
		try{
			SSHInterface.resetKnownHost();
			assertTrue("No host should be there",SSHInterface.getKnownHost().isEmpty());
			assertTrue("Fail to add localhost",SSHInterface.addKnownHost("localhost", 22) == null);
			assertTrue("Does not contains localhost",SSHInterface.getKnownHost().contains("localhost"));
			assertTrue("localhost is normally already inserted",SSHInterface.addKnownHost("localhost", 22) != null);
			assertTrue("Fail to add namenode server",SSHInterface.addKnownHost("namenode", 22) == null);
			assertTrue("List should still contain localhost",SSHInterface.getKnownHost().contains("localhost"));
			assertTrue("List should contain namenode", SSHInterface.getKnownHost().contains("namenode"));
			assertTrue("Fail to remove namenode",SSHInterface.removeKnownHost("namenode") == null);
			assertTrue("List should still contain localhost",SSHInterface.getKnownHost().contains("localhost"));
			assertTrue("List should not contain namenode anymore", !SSHInterface.getKnownHost().contains("namenode"));
			
			Map<String,DataStore> l = SSHInterface.getHosts();
			assertTrue(l.size() == 1);
			l.get("localhost").close();
			assertTrue("Fail to remove localhost",SSHInterface.removeKnownHost("localhost") == null);
			assertTrue("List should be empty",SSHInterface.getKnownHost().isEmpty());
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
		
	}
	
}