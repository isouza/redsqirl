package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.workflow.server.action.utils.TestUtils;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.action.HiveElement;
import com.redsqirl.workflow.server.action.HiveFilterInteraction;
import com.redsqirl.workflow.server.action.HiveSelect;
import com.redsqirl.workflow.server.action.HiveSource;
import com.redsqirl.workflow.server.action.Source;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;

public class ConditionInteractionTests {

	Logger logger = Logger.getLogger(getClass());
	
	Map<String,String> getColumns(){
		Map<String,String> ans = new HashMap<String,String>();
		ans.put(HiveInterface.key_columns,"ID STRING, VALUE INT");
		return ans;
	}
	
	public DataFlowElement getSource() throws RemoteException{
		HiveInterface hInt = new HiveInterface();
		String new_path1 = TestUtils.getTablePath(1);
		
		hInt.delete(new_path1);
		assertTrue("create "+new_path1,
				hInt.create(new_path1, getColumns()) == null
				);
		
		HiveSource src = new HiveSource();

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset).getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(new_path1);

		Tree<String> feat1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat1.add("name").add("ID");
		feat1.add("type").add("STRING");

		Tree<String> feat2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat2.add("name").add("VALUE");
		feat2.add("type").add("INT");
		
		String error = src.updateOut();
		assertTrue("source update: "+error,error == null);
		
		return src;
	}
	
	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			DataFlowElement src = getSource();
			HiveElement hs = new HiveSelect();
			src.setComponentId("1");
			hs.setComponentId("2");
			error = src.addOutputComponent(Source.out_name, hs);
			assertTrue("source add output: "+error,error == null);
			error = hs.addInputComponent(HiveSelect.key_input, src);
			assertTrue("hive select add input: "+error,error == null);
			
			HiveFilterInteraction ci = hs.getFilterInt();
			
			logger.debug(hs.getDFEInput());
			hs.update(ci);
			ci.setValue("VAL < 10");
			error = ci.check();
			assertTrue("check1: VAL does not exist : "+error,error != null);
			ci.setValue("VALUE < 10");
			error = ci.check();
			assertTrue("check2: "+error,error == null);
			
			
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
}