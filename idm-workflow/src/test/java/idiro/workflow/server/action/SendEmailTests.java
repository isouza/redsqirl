package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

public class SendEmailTests {

static Logger logger = Logger.getLogger(SendEmailTests.class);

	
	public static DataFlowElement createSendEmailWithSrc(
			DataFlow w,
			DataFlowElement src) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new SendEmail()).getName());
		logger.debug("convert: "+idHS);
		
		SendEmail conv = (SendEmail) w.getElement(idHS);
		
		logger.info(Source.out_name+" "+src.getComponentId());
		logger.debug(SendEmail.key_input+" "+idHS);
		
		error = w.addLink(
				Source.out_name, src.getComponentId(), 
				SendEmail.key_input, idHS);
		assertTrue("convert add link: "+error,error == null);
		
		updateSendEmail(w,conv);
		
		
		logger.debug("HS update out...");
		error = conv.updateOut();
		assertTrue("convert update: "+error,error == null);
		
		return conv;
	}
	

	
	public static void updateSendEmail(
			DataFlow w,
			SendEmail conv) throws RemoteException, Exception{
		
		logger.info("update convert...");
		
		logger.info("update format...");
		conv.getSubjectInt().setValue("test");
		
		logger.info("update properties...");
		conv.getDestinataryInt().setValue("marcos.freitas@idiro.com");
		
		logger.info("update properties...");
		conv.getMessageInt().setValue("body message");
		
		logger.info("Conv update out...");
		String error = conv.updateOut();
		assertTrue("convert update out: "+error,error == null);
	}
	
	
	
	@Test
	public void basic(){
		
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		HiveInterface hiveInt = null;
		HDFSInterface hdfsInt = null;
		
		String new_path1 =TestUtils.getTablePath(1);
		String new_path2 = TestUtils.getPath(2);
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			hiveInt = new HiveInterface();
			hdfsInt = new HDFSInterface();
			
			hiveInt.delete(new_path1);
			hdfsInt.delete(new_path2);
			
			DataFlowElement src = SourceTests.createSrc_ID_VALUE(w,hiveInt,new_path1);
			
			createSendEmailWithSrc(w,src);

			logger.info("run...");
			OozieClient wc = OozieManager.getInstance().getOc();
			logger.info("Got Oozie Client");
			error = w.run();
			assertTrue("Job submition failed: "+error, error == null);
			String jobId = w.getOozieJobId();
			if(jobId == null){
				assertTrue("jobId cannot be null", false);
			}
			logger.info(jobId);
			
			// wait until the workflow job finishes printing the status every 10 seconds
		    while(
		    		wc.getJobInfo(jobId).getStatus() == 
		    		org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
		    	System.out.println("Workflow job running ...");
		    	logger.info("Workflow job running ...");
		        Thread.sleep(10 * 1000);
		    }
		    logger.info("Workflow job completed ...");
		    logger.info(wc.getJobInfo(jobId));
		    error = wc.getJobInfo(jobId).toString();
		    assertTrue(error, error.contains("SUCCEEDED"));
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
		try{
			hiveInt.delete(new_path1);
			hdfsInt.delete(new_path2);
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
	
}